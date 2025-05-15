package de.monticore.lang.monticar.emadl.generator;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAComponentSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch.generator.CNNArchGenerator;
import de.monticore.lang.monticar.cnnarch.generator.CNNTrainGenerator;
import de.monticore.lang.monticar.cnnarch.generator.annotations.ArchitectureAdapter;
import de.monticore.lang.monticar.cnnarch.generator.decomposition.NetworkStructure;
import de.monticore.lang.monticar.cnnarch.generator.preprocessing.PreprocessingComponentParameterAdapter;
import de.monticore.lang.monticar.cnnarch.generator.preprocessing.PreprocessingPortChecker;
import de.monticore.lang.monticar.cnnarch.generator.training.TrainingComponentsContainer;
import de.monticore.lang.monticar.cnnarch.generator.training.TrainingConfiguration;
import de.monticore.lang.monticar.cnnarch.generator.validation.TrainedArchitectureChecker;
import de.monticore.lang.monticar.cnnarch.gluongenerator.CNNTrain2Gluon;
import de.monticore.lang.monticar.emadl._cocos.EMADLCocos;
import de.monticore.lang.monticar.emadl.generator.modularcnn.NetworkCompositionHandler;

import de.monticore.lang.monticar.generator.FileContent;
import de.monticore.lang.monticar.generator.pythonwrapper.GeneratorPythonWrapperStandaloneApi;
import de.monticore.lang.monticar.generator.pythonwrapper.symbolservices.data.ComponentPortInformation;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.se_rwth.commons.logging.Log;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

import static de.monticore.lang.monticar.cnnarch.generator.validation.Constants.ROOT_SCHEMA_MODEL_PATH;

public class CNNHandler {

    private CNNArchGenerator cnnArchGenerator;
    private CNNTrainGenerator cnnTrainGenerator;
    private EMADLGenerator generator;
    private FileHandler fileHandler;
    private Tagging taggingHandler;
    private GeneratorPythonWrapperStandaloneApi pythonWrapper;
    private String composedNetworkFilePath;
    private Map<String, ArchitectureSymbol> processedArch;
    private LinkedHashMap<String, ArchitectureSymbol> cachedComposedArchitectureSymbols = new LinkedHashMap<>();

    private LinkedHashMap<String, NetworkStructure> composedNetworkStructures = new LinkedHashMap();




    public CNNHandler(EMADLGenerator emadlGen, Map<String, ArchitectureSymbol> processedArch, GeneratorPythonWrapperStandaloneApi pythonWrapperApi, String composedNetworkFilePath ) {
        generator = emadlGen;
        cnnArchGenerator = generator.getBackend().getCNNArchGenerator();
        cnnTrainGenerator = generator.getBackend().getCNNTrainGenerator();
        fileHandler = generator.getEmadlFileHandler();
        taggingHandler = generator.getEmadlTaggingHandler();
        pythonWrapper = pythonWrapperApi;
        this.composedNetworkFilePath = composedNetworkFilePath;
        this.processedArch = processedArch;

    }

    public LinkedHashMap<String, ArchitectureSymbol> getCachedComposedArchitectureSymbols() {
        return this.cachedComposedArchitectureSymbols;
    }

    public LinkedHashMap<String, NetworkStructure> getComposedNetworkStructures() {
        return this.composedNetworkStructures;
    }

    public void setComposedNetworkStructures(LinkedHashMap<String, NetworkStructure> composedNetworkStructures) {
        this.composedNetworkStructures = composedNetworkStructures;
    }


    protected CNNArchGenerator getCnnArchGenerator() {
        return cnnArchGenerator;
    }

    protected  void generateCNN(List<FileContent> fileContents, TaggingResolver taggingResolver, EMAComponentInstanceSymbol instance, ArchitectureSymbol architecture) {
        List<FileContent> contents = cnnArchGenerator.generateStrings(taggingResolver, architecture);
        String fullName = instance.getFullName().replaceAll("\\.", "_");

        //get the components execute method
        String executeKey = "execute_" + fullName;
        List<String> executeMethods = fileHandler
                .getContentOf(contents, executeKey);
        if (executeMethods.size() != 1) {
            throw new IllegalStateException("execute method of " + fullName + " not found");
        }
        String executeMethod = executeMethods.get(0);
        contents.remove(executeKey);

        List<String> applyBeamSearchMethods = fileHandler.getContentOf(contents, "BeamSearch_" + fullName);
        String applyBeamSearchMethod = null;
        if (applyBeamSearchMethods.size() == 1) {
            applyBeamSearchMethod = applyBeamSearchMethods.get(0);
        }

        String component = generator.getEmamGen().generateString(taggingResolver, instance, null);
        FileContent componentFileContent = new FileContent(
                generator.transformComponent(component, "CNNPredictor_" + fullName,
                        applyBeamSearchMethod,
                        executeMethod,
                        architecture),
                instance);

        fileContents.addAll(contents);
        fileContents.add(componentFileContent);
        fileContents.add(new FileContent(fileHandler.readResource("CNNTranslator.h", Charsets.UTF_8), "CNNTranslator.h"));
    }

    protected List<FileContent> generateCNNTrainer(Set<EMAComponentInstanceSymbol> allInstances, String mainComponentName) {
        boolean copied = fileHandler.copySchemaFilesFromResource(ROOT_SCHEMA_MODEL_PATH);
        List<FileContent> fileContents = new ArrayList<>();
        TaggingResolver symTabAndTaggingResolver = taggingHandler.getSymTabAndTaggingResolver();


        NetworkCompositionHandler networkCompositionHandler = new NetworkCompositionHandler(this.composedNetworkFilePath, fileHandler.getModelsPath(), fileHandler.getInstanceVault(),
                this.cachedComposedArchitectureSymbols, this.generator.getBackend(), this.composedNetworkStructures);
        //composedNetworkHandler.refreshInformation(allInstances);
        //Set<EMAComponentInstanceSymbol> networks = composedNetworkHandler.getSortedNetworksFromAtomicToComposed(allInstances);
        ArrayList<EMAComponentInstanceSymbol> networks = networkCompositionHandler.processComponentInstances(allInstances);

        for (EMAComponentInstanceSymbol componentInstance : networks) {

            EMAComponentSymbol component = componentInstance.getComponentType().getReferencedSymbol();
            //Optional<ArchitectureSymbol> architecture = component.getSpannedScope().resolve("", ArchitectureSymbol.KIND);
            Optional<ArchitectureSymbol> architecture = null;

            architecture = networkCompositionHandler.resolveArchitectureSymbolOfReferencedSymbol(componentInstance);



            if (architecture != null && architecture.isPresent()) {
                String mainComponentConfigFilename = mainComponentName.replaceAll("\\.", "/");

                String componentConfigFilename = null;
                String instanceConfigFilename = null;

                if (networkCompositionHandler.isComposedNet(componentInstance)){

                    String rootNetworkName = networkCompositionHandler.findConfigFileName(componentInstance);
                    if (Strings.isNullOrEmpty(rootNetworkName)) {
                        String message = String.format("(Root) network config not found");
                        Log.error(message);
                        throw new RuntimeException(String.format("Missing training configuration for network '%s'", mainComponentName));
                    }
                    String packagePath = component.getFullName().replaceAll("\\." ,"/");
                    int lastIndex = packagePath.lastIndexOf("/");
                    packagePath = packagePath.substring(0,lastIndex+1) ;

                    componentConfigFilename = packagePath + rootNetworkName;
                    instanceConfigFilename = packagePath + rootNetworkName + "_" + rootNetworkName;

                } else {
                    componentConfigFilename = component.getFullName().replaceAll("\\.", "/");
                    instanceConfigFilename = component.getFullName().replaceAll("\\.", "/") + "_" + component.getName();
                }

                String trainConfigFilename = fileHandler.getConfigFilename(mainComponentConfigFilename, componentConfigFilename, instanceConfigFilename);
                if (Strings.isNullOrEmpty(trainConfigFilename)) {
                    if (!networkCompositionHandler.isComposedNetAndHasConfig(componentInstance) && networkCompositionHandler.isPartOfComposedNet(componentInstance)){
                        Log.info("Found part of composed net","COMPOSED_NET_PART");
                        continue;
                    } else if (networkCompositionHandler.isPartOfComposedNet(componentInstance)) {
                        continue;
                    } else {
                        String message = String.format("Missing training configuration. Could not find a file with any of the following names (only one needed): '%s.conf', '%s.conf', '%s.conf'. These files denote respectively the configuration for the single instance, the component or the whole system.",
                                fileHandler.getModelsPath() + instanceConfigFilename, fileHandler.getModelsPath() + componentConfigFilename, fileHandler.getModelsPath() + mainComponentConfigFilename);
                        Log.error(message);
                        throw new RuntimeException(String.format("Missing training configuration for network '%s'", mainComponentName));
                    }

                }

                cnnTrainGenerator.setGenerationTargetPath(generator.getGenerationTargetPath());
                if (cnnTrainGenerator instanceof CNNTrain2Gluon) {
                    ((CNNTrain2Gluon) cnnTrainGenerator).setRootProjectModelsDir(fileHandler.getModelsPath());
                }
                List<String> names = Splitter.on("/").splitToList(trainConfigFilename);
                trainConfigFilename = names.get(names.size() - 1);
                Path modelPath = Paths.get(fileHandler.getModelsPath() + Joiner.on("/").join(names.subList(0, names.size() - 1)));

                Log.info("Training comp: " + componentConfigFilename,"CONFIG_CNN_TRAIN");

                TrainingConfiguration trainingConfiguration = cnnTrainGenerator.createTrainingConfiguration(modelPath,
                        trainConfigFilename, copied ? Paths.get(generator.getGenerationTargetPath()) : null);

                // Annotate train configuration with architecture
                final String fullConfigName = String.join(".", names);
                Map<String,ArchitectureSymbol> processedArch = generator.getProcessedArchitecture();

                ArchitectureSymbol correspondingArchitecture = generator.getProcessedArchitecture().get(fullConfigName);

                if (correspondingArchitecture == null) {
                    correspondingArchitecture = architecture.get();
                }

                assert correspondingArchitecture != null : "No architecture found for train " + fullConfigName + " configuration!";



                TrainingComponentsContainer trainingComponentsContainer = new TrainingComponentsContainer();
                trainingComponentsContainer.setTrainedArchitecture(trainingConfiguration,
                        new ArchitectureAdapter(correspondingArchitecture));
                TrainedArchitectureChecker.checkComponents(trainingConfiguration, trainingComponentsContainer);


                    // Resolve critic network if critic is present
                    Optional<String> criticName = trainingConfiguration.getCritic();
                    if (criticName.isPresent()) {
                        String fullCriticName = criticName.get();
                        int indexOfFirstNameCharacter = fullCriticName.lastIndexOf('.') + 1;
                        fullCriticName = fullCriticName.substring(0, indexOfFirstNameCharacter)
                                + fullCriticName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                                + fullCriticName.substring(indexOfFirstNameCharacter + 1);

                        EMAComponentInstanceSymbol instanceSymbol = generator.resolveComponentInstanceSymbol(fullCriticName,
                                symTabAndTaggingResolver);
                        EMADLCocos.checkAll(instanceSymbol);
                        Optional<ArchitectureSymbol> critic = networkCompositionHandler.resolveArchitectureSymbolOfInstance(instanceSymbol);
                        if (!critic.isPresent()) {
                            String message = String.format("Resolving critic component failed. Critic component '%s' does not have a CNN implementation, but is required to have one.", fullCriticName);
                            Log.error(message);
                            throw new RuntimeException(message);
                        }
                        ArchitectureSymbol architectureSymbol = critic.get();
                        architectureSymbol.setComponentName(fullCriticName);
                        trainingComponentsContainer.setCriticNetwork(new ArchitectureAdapter(architectureSymbol));
                    }

                    // Resolve discriminator network if discriminator is present
                    Optional<String> discriminatorName = trainingConfiguration.getDiscriminatorName();
                    if (discriminatorName.isPresent()) {
                        String fullDiscriminatorName = discriminatorName.get();
                        int indexOfFirstNameCharacter = fullDiscriminatorName.lastIndexOf('.') + 1;
                        fullDiscriminatorName = fullDiscriminatorName.substring(0, indexOfFirstNameCharacter)
                                + fullDiscriminatorName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                                + fullDiscriminatorName.substring(indexOfFirstNameCharacter + 1);

                        EMAComponentInstanceSymbol instanceSymbol = generator.resolveComponentInstanceSymbol(
                                fullDiscriminatorName, taggingHandler.getSymTabAndTaggingResolver());
                        EMADLCocos.checkAll(instanceSymbol);
                        Optional<ArchitectureSymbol> discriminator = networkCompositionHandler.resolveArchitectureSymbolOfInstance(instanceSymbol);
                        if (!discriminator.isPresent()) {
                            String message = String.format("Resolving discriminator component failed. Discriminator component '%s' does not have a CNN implementation, but is required to have one.", fullDiscriminatorName);
                            Log.error(message);
                            throw new RuntimeException(message);
                        }
                        ArchitectureSymbol architectureSymbol = discriminator.get();
                        architectureSymbol.setComponentName(fullDiscriminatorName);
                        trainingComponentsContainer.setDiscriminatorNetwork(
                                new ArchitectureAdapter(architectureSymbol));
                    }

                    // Resolve QNetwork if present
                    Optional<String> qNetworkName = trainingConfiguration.getQNetwork();
                    if (qNetworkName.isPresent()) {
                        String fullQNetworkName =  qNetworkName.get();
                        int indexOfFirstNameCharacter = fullQNetworkName.lastIndexOf('.') + 1;
                        fullQNetworkName = fullQNetworkName.substring(0, indexOfFirstNameCharacter)
                                + fullQNetworkName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                                + fullQNetworkName.substring(indexOfFirstNameCharacter + 1);

                        EMAComponentInstanceSymbol instanceSymbol = generator.resolveComponentInstanceSymbol(
                                fullQNetworkName, taggingHandler.getSymTabAndTaggingResolver());
                        EMADLCocos.checkAll(instanceSymbol);
                        Optional<ArchitectureSymbol> qNetwork = networkCompositionHandler.resolveArchitectureSymbolOfInstance(instanceSymbol);
                        if (!qNetwork.isPresent()) {
                            String message = String.format("Resolving Q-Network component failed. Q-Network component '%s' does not have a CNN implementation, but is required to have one.", fullQNetworkName);
                            Log.error(message);
                            throw new RuntimeException(message);
                        }
                        ArchitectureSymbol architectureSymbol = qNetwork.get();
                        architectureSymbol.setComponentName(fullQNetworkName);
                        trainingComponentsContainer.setQNetwork(new ArchitectureAdapter(architectureSymbol));
                    }

                    // Resolve encoder network if present
                    Optional<String> encoderName = trainingConfiguration.getEncoderName();
                    if (encoderName.isPresent()) {
                        String fullEncoderName = encoderName.get();
                        int indexOfFirstNameCharacter = fullEncoderName.lastIndexOf('.') + 1;
                        fullEncoderName = fullEncoderName.substring(0, indexOfFirstNameCharacter)
                                + fullEncoderName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                                + fullEncoderName.substring(indexOfFirstNameCharacter + 1);

                        EMAComponentInstanceSymbol instanceSymbol = generator.resolveComponentInstanceSymbol(
                                fullEncoderName, taggingHandler.getSymTabAndTaggingResolver());
                        EMADLCocos.checkAll(instanceSymbol);
                        Optional<ArchitectureSymbol> encoder = networkCompositionHandler.resolveArchitectureSymbolOfInstance(instanceSymbol);
                        if (!encoder.isPresent()) {
                            String message = String.format("Resolving encoder component failed. Encoder component '%s' does not have a CNN implementation, but is required to have one.", fullEncoderName);
                            Log.error(message);
                            throw new RuntimeException(message);
                        }
                        ArchitectureSymbol architectureSymbol = encoder.get();
                        architectureSymbol.setComponentName(fullEncoderName);
                        trainingComponentsContainer.setEncoderNetwork(
                                new ArchitectureAdapter(architectureSymbol));

                        ArchitectureAdapter decoder = trainingComponentsContainer.getTrainedArchitecture().get();
                        ArchitectureSymbol decoderArchitectureSymbol = decoder.getArchitectureSymbol();

                        encoder.get().setAuxiliaryArchitecture(decoderArchitectureSymbol);

                    }

                    Optional<String> rewardFunctionNameOpt = trainingConfiguration.getRewardFunctionName();
                    if (rewardFunctionNameOpt.isPresent()) {
                        String fullRewardFunctionName =  rewardFunctionNameOpt.get();
                        int indexOfFirstNameCharacter = fullRewardFunctionName.lastIndexOf('.') + 1;
                        fullRewardFunctionName = fullRewardFunctionName.substring(0, indexOfFirstNameCharacter)
                                + fullRewardFunctionName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                                + fullRewardFunctionName.substring(indexOfFirstNameCharacter + 1);

                        EMAComponentInstanceSymbol instanceSymbol = generator.resolveComponentInstanceSymbol(
                                fullRewardFunctionName, taggingHandler.getSymTabAndTaggingResolver());
                        EMADLCocos.checkAll(instanceSymbol);
                        trainingComponentsContainer.setRewardFunction(instanceSymbol);
                    }

                    Optional<String> policyFunctionNameOpt = trainingConfiguration.getPolicyFunctionName();
                    if (policyFunctionNameOpt.isPresent()) {
                        String fullPolicyFunctionName =  policyFunctionNameOpt.get();
                        int indexOfFirstNameCharacter = fullPolicyFunctionName.lastIndexOf('.') + 1;
                        fullPolicyFunctionName = fullPolicyFunctionName.substring(0, indexOfFirstNameCharacter)
                                + fullPolicyFunctionName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                                + fullPolicyFunctionName.substring(indexOfFirstNameCharacter + 1);

                        EMAComponentInstanceSymbol instanceSymbol = generator.resolveComponentInstanceSymbol(
                                fullPolicyFunctionName, taggingHandler.getSymTabAndTaggingResolver());
                        EMADLCocos.checkAll(instanceSymbol);
                        trainingComponentsContainer.addTrainingComponent("policy",
                                instanceSymbol.getComponentType().getReferencedSymbol());
                    }

                    Optional<String> preprocessor = trainingConfiguration.getPreprocessor();
                    if (preprocessor.isPresent()) {
                        String fullPreprocessorName = preprocessor.get();

                        int indexOfFirstNameCharacter = fullPreprocessorName.lastIndexOf('.') + 1;
                        fullPreprocessorName = fullPreprocessorName.substring(0, indexOfFirstNameCharacter)
                                + fullPreprocessorName.substring(indexOfFirstNameCharacter, indexOfFirstNameCharacter + 1).toUpperCase()
                                + fullPreprocessorName.substring(indexOfFirstNameCharacter + 1);
                        String instanceName = componentInstance.getFullName().replaceAll("\\.", "_");

                        TaggingResolver symtab = taggingHandler.getSymTabAndTaggingResolver();
                        EMAComponentInstanceSymbol processor_instance = generator.resolveComponentInstanceSymbol(fullPreprocessorName, symtab);
                        processor_instance.setFullName("CNNPreprocessor_" + instanceName);
                        List<FileContent> processorContents = new ArrayList<>();
                        generator.generateComponent(processorContents, new HashSet<>(), symtab, processor_instance);
                        fileHandler.fixArmadilloImports(processorContents);

                        for (FileContent fileContent : processorContents) {
                            try {
                                generator.getEmamGen().generateFile(fileContent);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        String targetPath = generator.getGenerationTargetPath();
                        ComponentPortInformation componentPortInformation;
                        componentPortInformation = pythonWrapper.generateAndTryBuilding(processor_instance, targetPath + "/pythonWrapper", targetPath);
                        PreprocessingComponentParameterAdapter componentParameter = new PreprocessingComponentParameterAdapter(componentPortInformation);
                        PreprocessingPortChecker.check(componentParameter);
                        trainingComponentsContainer.setPreprocessingComponentParameter(componentParameter);
                    }

                    String cnnTrainGenInstanceName = componentInstance.getFullName().replaceAll("\\.", "_");

                    cnnTrainGenerator.setInstanceName(cnnTrainGenInstanceName);
                        List<FileContent> fileContentList = cnnTrainGenerator.generateStrings(trainingConfiguration,
                                trainingComponentsContainer, copied ? Paths.get(generator.getGenerationTargetPath()) : null);
                        fileContents.addAll(fileContentList);
            }
        }

        return fileContents;
    }
}
