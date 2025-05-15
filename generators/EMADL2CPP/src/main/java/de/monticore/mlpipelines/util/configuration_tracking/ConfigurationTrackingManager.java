package de.monticore.mlpipelines.util.configuration_tracking;


import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.*;
import de.monticore.mlpipelines.automl.emadlprinter.EmadlPrettyPrinter;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;
import de.monticore.mlpipelines.pipelines.Pipeline;
import de.se_rwth.commons.logging.Log;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigurationTrackingManager {
    private static int runId = 0;
    private static ArtifactManager artifactManager;
    private static Artifact artifact;
    private static boolean deployArtifact = false;


    public static void applyConfiguration(Pipeline trainPipeline, MontiAnnaContext montiAnnaContext) {
        ConfigurationTrackingConf.setModelName(montiAnnaContext.getRootModelName());
        ConfigurationTrackingConf.setModelPath(montiAnnaContext.getParentModelPath().toString());
        artifact = new Artifact();
        artifact.setConfiguration("training_configuration", trainPipeline.getTrainingConfiguration());
        artifact.setConfigurationEntry("info", "model", montiAnnaContext.getRootModelName());
        artifact.setConfigurationEntry("info", "experiment_name", ConfigurationTrackingConf.getExperimentName());
        artifact.setConfigurationEntry("info", "network_name", trainPipeline.getNeuralNetwork().getComponentType().getName());
        loadDatasetInfo();
        artifactManager = new ArtifactManager(artifact);
    }

    public static void applyConfiguration(ASTConfLangCompilationUnit nasConf, ASTConfLangCompilationUnit searchSpace, ASTConfLangCompilationUnit hyperparamsOptConf, ASTConfLangCompilationUnit evaluationCriteria, Pipeline trainPipeline, MontiAnnaContext montiAnnaContext) {
        applyConfiguration(trainPipeline, montiAnnaContext);
        artifact.setConfiguration("nas_configuration", nasConf);
        artifact.setConfiguration("ho_configuration", hyperparamsOptConf);
        artifact.setConfiguration("evaluation_criteria", evaluationCriteria);
        artifact.setConfiguration("search_space", searchSpace);
        artifactManager = new ArtifactManager(artifact);
    }

    public static void loadDatasetInfo() {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(String.format("%s/%s", ConfigurationTrackingConf.getPathTmp(), ConfigurationTrackingConf.getDatasetFileName())));
            String dataset = reader.readLine();
            artifact.setConfigurationEntry("info", "dataset", dataset);
        } catch (IOException e) {
            Log.info(String.format("The dataset info could not be loaded from %s. Error: %s", ConfigurationTrackingConf.getPathTmp(), e.getMessage()), "CONFIGURATION_TRACKING");
        }
    }

    public static void setArtifactConfigurationEntry(String configurationKey, String key, String value) {
        artifact.setConfigurationEntry(configurationKey, key, value);
    }

    public static int getRunId() {
        return runId;
    }

    public static Artifact getArtifact() {
        return artifact;
    }

    public static void setArtifact(Artifact artifact1) {
        artifact = artifact1;
    }

    private static String getTargetPath(String pathTmp, String experimentName) {
        if (experimentName == null || experimentName.isEmpty()) {
            String uuidRegex = "[a-f0-9]{8}-([a-f0-9]{4}){2}-[a-f0-9]{12}";
            for (File file : Objects.requireNonNull(new File(pathTmp).listFiles())) {
                if (file.isDirectory() && file.getName().matches(uuidRegex)) {
                    return file.getPath();
                }
            }
        }
        return String.format("%s/%s", pathTmp, experimentName);
    }

    public static void initializeNewRun(Pipeline pipeline, String stage) {
        artifact.setConfiguration("training_configuration", pipeline.getPipelineConfiguration());
        artifact.setConfigurationEntry("info", "run_name", String.format("Run %d", ++runId));
        artifact.setConfigurationEntry("info", "stage", stage);
        artifact.setNetworkString(getNetworkString(pipeline.getNeuralNetwork()));
        artifact.setConfigurationEntry("info", "train_accuracy", "0");
        artifactManager.setSimilarArtifacts(new ArrayList<>());
    }

    public static void saveRun(Pipeline trainPipeline) {
        if (!ConfigurationTrackingConf.isEnabled()) {
            return;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
        artifact.setConfigurationEntry("info", "run_at", LocalDateTime.now().format(formatter));
        artifact.setConfiguration("training_configuration", trainPipeline.getTrainingConfiguration());
        String targetPath = getTargetPath(ConfigurationTrackingConf.getPathTmp(), ConfigurationTrackingConf.getExperimentName());
        artifact.saveArtifact(targetPath);
    }

    public static void executePipeline(Pipeline pipeline, String stage) {
        if (!ConfigurationTrackingConf.isEnabled()) {
            pipeline.execute();
            return;
        }

        initializeNewRun(pipeline, stage);
        if (configurationNotRun()) {
            pipeline.execute();
            artifact.setAccuracy(pipeline.getTrainedAccuracy());
            ConfigurationTrackingReport.logPipeline(pipeline);
        }
        saveRun(pipeline);
        if (stage.equals("Final Run") || stage.equals("MontiAnna Run")) {
            ConfigurationTrackingReport.saveReport();
        }
    }

    public static boolean configurationNotRun() {
        ConfigurationTrackingReport.logInitializeCheck(artifact);
        if (artifactManager.hasSimilarArtifacts(artifact, "GitLab") || artifactManager.hasSimilarArtifacts(artifact, "Mlflow")) {
            return false;
        }
        deployArtifact = true;
        ConfigurationTrackingReport.logMessage("No similar artifacts were found, training model...");
        return true;
    }

    public static boolean shouldDeployArtifact() {
        return deployArtifact;
    }

    private static String getNetworkString(EMAComponentInstanceSymbol componentInstanceSymbol) {
        ArchitectureSymbol architectureSymbol = (ArchitectureSymbol) componentInstanceSymbol.getSpannedScope().getSubScopes().get(0).getSpanningSymbol().get();
        String networkStr = new EmadlPrettyPrinter().prettyPrint(architectureSymbol)
                .replaceAll("component.*<", String.format("component %s<", artifact.getInfoValue("network_name")))
                .replace(", ;", ";");
        return String.format("package %s;\n\n%s", componentInstanceSymbol.getPackageName().split("\\.")[0], networkStr);
    }
}
