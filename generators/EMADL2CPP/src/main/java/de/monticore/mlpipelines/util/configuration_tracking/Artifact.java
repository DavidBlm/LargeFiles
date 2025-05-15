package de.monticore.mlpipelines.util.configuration_tracking;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.io.paths.ModelPath;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._ast.ASTEMACompilationUnit;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAComponentSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.emadl._symboltable.EMADLLanguage;
import de.monticore.mlpipelines.automl.emadlprinter.ASTConfLangCompilationUnitPrinter;
import de.monticore.mlpipelines.automl.emadlprinter.EmadlPrettyPrinter;
import de.monticore.mlpipelines.tracking.helper.ASTConfLangHelper;
import de.monticore.parsing.EMADLParser;
import de.monticore.symbolmanagement.SymbolTableCreator;
import de.monticore.symboltable.GlobalScope;
import de.monticore.symboltable.Scope;
import org.apache.commons.lang3.StringUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

public class Artifact {
    private Map<String, ASTConfLangCompilationUnit> artifactConfiguration;
    private Map<String, Map<String, String>> artifact;
    private ArchitectureSymbol network;
    private float similarityScore;

    public Artifact() {
        artifactConfiguration = new TreeMap<>();
        artifact = new TreeMap<>();
        similarityScore = 0;
    }

    public Map<String, Map<String, String>> getArtifact() {
        return artifact;
    }

    public void setArtifact(Map<String, Map<String, String>> configuration) {
        artifact = configuration;
        if (getInfoValue("network") != null) {
            setNetworkString(getInfoValue("network"));
        }
    }

    public Map<String, String> get(String key) {
        return artifact.get(key);
    }

    public String getArtifactName() {
        return String.format("%s_%s_%s", getInfoValue("experiment_name"), getInfoValue("run_name"), getInfoValue("stage"));
    }

    public String getExperimentName() {
        return getInfoValue("experiment_name");
    }

    public String getInfoValue(String key) {
        if (artifact.get("info") != null) {
            return artifact.get("info").get(key);
        }
        return null;
    }

    public void put(String key, Map<String, String> value) {
        if (artifact.get(key) != null) {
            artifact.get(key).putAll(value);
        } else {
            artifact.put(key, value);
        }
    }

    public ASTConfLangCompilationUnit getConfiguration(String configurationName) {
        return artifactConfiguration.get(configurationName);
    }

    public void setConfiguration(String configurationName, ASTConfLangCompilationUnit astConfiguration) {
        artifactConfiguration.put(configurationName, astConfiguration);
        this.put(configurationName, new TreeMap<>(ASTConfLangHelper.getParametersFromConfiguration(astConfiguration)));
    }

    public ArchitectureSymbol getNetwork() {
        return network;
    }

    public void setNetworkString(String networkString) {
        network = parseArchitectureSymbol_String(networkString);
        artifact.get("info").put("network", networkString);
    }

    public void setConfigurationEntry(String configurationName, String key, String value) {
        Map<String, String> conf = artifact.get(configurationName);
        if (conf == null) {
            conf = new TreeMap<>();
        }
        conf.put(key, value);
        artifact.put(configurationName, conf);
    }

    public String toString() {
        return artifact.entrySet().toString();
    }

    public float getAccuracy() {
        if (getInfoValue("train_accuracy") != null) {
            return Float.parseFloat(getInfoValue("train_accuracy"));
        }
        return 0;
    }

    public void setAccuracy(float accuracy) {
        artifact.get("info").put("train_accuracy", Float.valueOf(accuracy).toString());
    }

    public void setSimilarityScore(float similarityScore) {
        this.similarityScore = similarityScore;
    }

    public float getSimilarityScore() {
        return similarityScore;
    }

    public String getTrainAlgorithmName() {
        if (artifact.get("training_configuration") != null) {
            return artifact.get("training_configuration").get("train_algorithm_name");
        }
        return null;
    }

    public void saveArtifact(String targetPath) {
        try {
            Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();         // disableHtmlEscaping due to encoding issues with network
            FileWriter fileWriter = new FileWriter(String.format("%s/%s.json", targetPath, getInfoValue("run_name")));
            gson.toJson(artifact, fileWriter);
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (String configurationName : artifactConfiguration.keySet()) {
            try {
                FileWriter fileWriter = new FileWriter(String.format("%s/%s_%s.conf", targetPath, getInfoValue("run_name"), configurationName));
                fileWriter.write(new ASTConfLangCompilationUnitPrinter().prettyPrint(artifactConfiguration.get(configurationName)));
                fileWriter.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            FileWriter fileWriter = new FileWriter(String.format("%s/%s_network.emadl", targetPath, getInfoValue("run_name")));
            fileWriter.write(new EmadlPrettyPrinter().prettyPrint(network));
            fileWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveNetworkIntoFile(String network, String networkName) {
        String tempName = "temp.emadl";
        File networkFile = new File(String.format("%s/%s/%s.emadl", ConfigurationTrackingConf.getModelPath(), ConfigurationTrackingConf.getModelName().split("\\.")[0], networkName));
        if (networkFile.exists()) {
            File tempFile = new File(String.format("%s/%s", networkFile.getParentFile().getPath(), tempName));
            boolean renamed = networkFile.renameTo(tempFile);
            if (renamed) {
                try {
                    FileWriter networkFileWriter = new FileWriter(String.format("%s/%s.emadl", networkFile.getParentFile().getPath(), networkName));
                    networkFileWriter.write(network);
                    networkFileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void resetNetworkFile(String networkName) {
        String tempName = "temp.emadl";
        File networkFile = new File(String.format("%s/%s/%s.emadl", ConfigurationTrackingConf.getModelPath(), ConfigurationTrackingConf.getModelName().split("\\.")[0], networkName));
        File tempFile = new File(String.format("%s/%s/%s", ConfigurationTrackingConf.getModelPath(), ConfigurationTrackingConf.getModelName().split("\\.")[0], tempName));
        if (tempFile.exists()) {
            tempFile.renameTo(new File(String.format("%s/%s.emadl", networkFile.getParentFile().getPath(), networkName)));
        }
    }

    private ArchitectureSymbol resolveNetwork(String networkName) {
        String rootComponentPath = Paths.get(ConfigurationTrackingConf.getModelPath(), getInfoValue("model").replaceFirst("\\.", "/") + ".emadl").toString();
        ModelPath modelPath_ = new ModelPath(Paths.get(ConfigurationTrackingConf.getModelPath()));
        ASTEMACompilationUnit rootComponent = null;
        try {
            rootComponent = new EMADLParser().parseModelIfExists(rootComponentPath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (rootComponent != null) {
            Scope emadlSymbolTable = SymbolTableCreator.createEMADLSymbolTable(rootComponent, new GlobalScope(modelPath_, new EMADLLanguage()));
            String rootComponentName = rootComponent.getComponent().getName();
            String instanceName = rootComponentName.substring(0, 1).toLowerCase() + rootComponentName.substring(1);
            EMAComponentInstanceSymbol componentInstance = (EMAComponentInstanceSymbol) emadlSymbolTable.resolve(
                    instanceName, EMAComponentInstanceSymbol.KIND).get();
            for (EMAComponentInstanceSymbol subcomponent : componentInstance.getSubComponents()) {
                EMAComponentSymbol referencedSymbol = subcomponent.getComponentType().getReferencedSymbol();
                Optional<ArchitectureSymbol> network_ = referencedSymbol.getSpannedScope()
                        .resolve("", ArchitectureSymbol.KIND);
                if (network_.isPresent() && network_.get().getEnclosingScope().getEnclosingScope().get().getName().get().equals(networkName)) {
                    return network_.get();
                }
            }
        }
        return null;
    }

    private ArchitectureSymbol parseArchitectureSymbol_String(String network) {
        String networkName = StringUtils.capitalize(network.split("<")[0].split("component ")[1].trim());
        saveNetworkIntoFile(network,networkName);
        ArchitectureSymbol architectureSymbol = resolveNetwork(networkName);
        resetNetworkFile(networkName);
        return architectureSymbol;
    }
}
