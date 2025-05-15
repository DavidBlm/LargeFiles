package de.monticore.mlpipelines.workflow;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.io.paths.ModelPath;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._ast.ASTEMACompilationUnit;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._ast.ASTSubComponent;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAComponentSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;
import de.monticore.lang.monticar.emadl._symboltable.EMADLLanguage;
import de.monticore.lang.monticar.semantics.Constants;
import de.monticore.lang.monticar.semantics.ExecutionSemantics;
import de.monticore.lang.monticar.semantics.construct.SymtabCreator;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.monticore.mlpipelines.automl.helper.ConfigurationValidationHandler;
import de.monticore.mlpipelines.backend.generation.MontiAnnaGenerator;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;
import de.monticore.mlpipelines.pipelines.executor.PipelineExecutor;
import de.monticore.parsing.ConfigurationLanguageParser;
import de.monticore.parsing.EMADLParser;
import de.monticore.symbolmanagement.SymbolTableCreator;
import de.monticore.symboltable.GlobalScope;
import de.monticore.symboltable.Scope;
import de.monticore.types.types._ast.ASTSimpleReferenceType;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.StringTransformations;
import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.io.FileUtils;

public abstract class AbstractWorkflow {

    protected final MontiAnnaContext montiAnnaContext;
    protected MontiAnnaGenerator montiAnnaGenerator;

    protected PipelineExecutor pipelineExecutor;

    protected String schemasTargetDir = "target/generated-sources/schemas/";

    public AbstractWorkflow(final MontiAnnaContext montiAnnaContext) {
        this.montiAnnaContext = montiAnnaContext;
        this.montiAnnaGenerator = new MontiAnnaGenerator(montiAnnaContext);
    }

    public AbstractWorkflow() {
        this.montiAnnaContext = MontiAnnaContext.getInstance();
        this.montiAnnaGenerator = new MontiAnnaGenerator(this.montiAnnaContext);
    }

    public void setMontiAnnaGenerator(final MontiAnnaGenerator montiAnnaGenerator) {
        this.montiAnnaGenerator = montiAnnaGenerator;
    }

    public void setPipelineExecutor(final PipelineExecutor pipelineExecutor) {
        this.pipelineExecutor = pipelineExecutor;
    }

    public void execute() throws IOException {
        // Measure the time for the whole function
        final long startTime = System.nanoTime();

        copyPythonScriptsToTarget();

        // Add schemas for configuration validation into target
        this.copySchemasToTarget();

        // frontend
        final String rootModelName = Names.getSimpleName(montiAnnaContext.getRootModelName());
        final String pathToModelsDirectory = Paths.get(montiAnnaContext.getParentModelPath().toString(),
                getDirectoryPathSupplementFromComponentName(montiAnnaContext.getRootModelName())).toString();
        final String pathToRootModel = Paths.get(pathToModelsDirectory, rootModelName + ".emadl").toString();
        final ASTEMACompilationUnit rootEMADLComponent = new EMADLParser().parseModelIfExists(pathToRootModel);
        final ModelPath modelPath = new ModelPath(montiAnnaContext.getParentModelPath());
        final Scope emadlSymbolTable = SymbolTableCreator.createEMADLSymbolTable(rootEMADLComponent,
                new GlobalScope(modelPath, new EMADLLanguage()));

        checkCoCos();
        backendSpecificValidations();
        final EMAComponentInstanceSymbol pipelineModelWithExecutionSemantics = calculateExecutionSemantics();

        generateBackendArtefactsIntoExperiment();
        createPipelineExecutor(LearningMethod.SUPERVISED);

        pipelineExecutor.setMontiAnnaContext(montiAnnaContext);
        pipelineExecutor.setSchemasTargetDir(schemasTargetDir);
        pipelineExecutor.setNetworkExecutionConfigs(
                this.getNetworkInstanceConfigs(pathToModelsDirectory, rootModelName, rootEMADLComponent, emadlSymbolTable)
        );
        pipelineExecutor.setPipelineModelWithExecutionSemantics(pipelineModelWithExecutionSemantics);
        pipelineExecutor.addEmadlFiles(getModelEmadlFiles(rootEMADLComponent, pathToRootModel));

        final long midTime = System.nanoTime();

        System.out.println("Monti time: " + (midTime - startTime) / 1000000 + "ms");

        pipelineExecutor.execute();

        final long endTime = System.nanoTime();
        System.out.println("Total time: " + (endTime - startTime) / 1000000 + "ms");
        System.out.println("Monti time: " + (midTime - startTime) / 1000000 + "ms");
        System.out.println("ML time: " + (endTime - midTime) / 1000000 + "ms");
    }

    protected String getDirectoryPathSupplementFromComponentName(final String rootModelName) {
        final int lastIndexOfDot = rootModelName.lastIndexOf(".");
        if (lastIndexOfDot == -1)
            return rootModelName;
        return rootModelName.substring(0, lastIndexOfDot).replace(".", "/");
    }

    protected List<EMAComponentInstanceSymbol> getAllNetworkInstances(
            final ASTEMACompilationUnit rootEMADLComponent,
            Scope emadlSymbolTable) {
        List<EMAComponentInstanceSymbol> networkInstancesList = new ArrayList<>();
        final String componentName = rootEMADLComponent.getComponent().getName();
        final String instanceName = componentName.substring(0, 1).toLowerCase() + componentName.substring(1);
        final EMAComponentInstanceSymbol componentInstance = (EMAComponentInstanceSymbol) emadlSymbolTable.resolve(
                instanceName, EMAComponentInstanceSymbol.KIND).get();
        for (final EMAComponentInstanceSymbol subcomponent : componentInstance.getSubComponents()) {
            final EMAComponentSymbol referencedSymbol = subcomponent.getComponentType().getReferencedSymbol();
            final Optional<ArchitectureSymbol> network = referencedSymbol.getSpannedScope()
                    .resolve("", ArchitectureSymbol.KIND);
            if (network.isPresent()) {
                networkInstancesList.add(subcomponent);
            }
        }
        return networkInstancesList;
    }

    protected List<Map<String, Object>> getNetworkInstanceConfigs(
            String pathToModelsDirectory, String rootModelName,
            final ASTEMACompilationUnit rootEMADLComponent,
            Scope emadlSymbolTable) throws IOException {
        List<Map<String, Object>> networkInstancesConfigs = new ArrayList<>();

        List<EMAComponentInstanceSymbol> networkInstances = getAllNetworkInstances(rootEMADLComponent, emadlSymbolTable);

        for (EMAComponentInstanceSymbol instanceSymbol : networkInstances) {
            Map<String, Object> configMap = new HashMap<>();

            String instanceNetworkName = this.getNetworkName(instanceSymbol);
            String instanceName = instanceSymbol.getName();
            String componentTypeName = instanceSymbol.getComponentType().getName();

            configMap.put("network", instanceSymbol);
            configMap.put("networkName", instanceNetworkName);

            // Training configuration:
            ASTConfLangCompilationUnit instanceTrainingConfiguration = this.getInstanceConfiguration(
                    pathToModelsDirectory, rootModelName, instanceName, componentTypeName, instanceNetworkName + ".conf"
            );
            ConfigurationValidationHandler.validateConfiguration(instanceTrainingConfiguration, this.pipelineExecutor.getSchemasTargetDir());
            configMap.put("trainingConfiguration", instanceTrainingConfiguration);

            // Pipeline configuration:
            ASTConfLangCompilationUnit pipelineConfiguration = this.getInstanceConfiguration(
                    pathToModelsDirectory, rootModelName, instanceName, componentTypeName, instanceNetworkName + "_pipeline.conf"
            );
            configMap.put("pipelineConfiguration", pipelineConfiguration);

            // Workflow specific configurations:
            addWorkflowSpecificConfigs(configMap, instanceName, componentTypeName, pathToModelsDirectory, rootModelName);

            networkInstancesConfigs.add(configMap);
        }
        return networkInstancesConfigs;
    }

    /**
     * Override to add workflow specific configuration entries to the network instance configuration.
     * This method does nothing by default.
     * @param configMap The configuration map for the network instance
     * @param instanceName The name of the network instance
     * @param componentTypeName The name of the component type
     * @param pathToModelsDirectory  The path to the models directory
     * @param rootModelName The name of the root model
     */
    protected void addWorkflowSpecificConfigs(Map<String, Object> configMap,
            String instanceName,
            String componentTypeName,
            String pathToModelsDirectory,
            String rootModelName) throws IOException {

    }

    //TODO implement me
    protected void checkCoCos() {

    }

    //TODO implement me
    protected void backendSpecificValidations() {
    }

    protected EMAComponentInstanceSymbol calculateExecutionSemantics() throws IOException {
        //TODO get from schema
        final String pathToPipelineReferenceModel = Paths.get(
                montiAnnaContext.getPipelineReferenceModelsPath().toString(), "LinearPipeline.ema").toString();
        EMAComponentInstanceSymbol pipelineReferenceModel = parsePipelineReferenceModelToEMAComponent(
                pathToPipelineReferenceModel);
        return addExecutionSemanticsToEmaComponent(
                pipelineReferenceModel);
    }

    public void generateBackendArtefactsIntoExperiment() {
        montiAnnaGenerator.generateTargetBackendArtefacts();
    }

    public abstract void createPipelineExecutor(final LearningMethod learningMethod);

    public EMAComponentInstanceSymbol parsePipelineReferenceModelToEMAComponent(final String pathToPipeline)
            throws IOException {
        final ASTEMACompilationUnit astemaCompilationUnit = new EMADLParser().parseModelIfExists(
                pathToPipeline);
        final ModelPath modelPath = new ModelPath(Paths.get("src/test/resources/models/pipeline"));
        final Scope pipelineSymbolTable = SymbolTableCreator.createEMADLSymbolTable(astemaCompilationUnit,
                new GlobalScope(modelPath, new EMADLLanguage()));
        final String pipelineName = astemaCompilationUnit.getComponent().getName();
        final Optional<EMAComponentInstanceSymbol> emaInstanceComponent = pipelineSymbolTable.resolve(
                StringTransformations.uncapitalize(pipelineName), EMAComponentInstanceSymbol.KIND);
        return emaInstanceComponent.orElseThrow(IllegalStateException::new);
    }

    public EMAComponentInstanceSymbol addExecutionSemanticsToEmaComponent(EMAComponentInstanceSymbol pipelineReferenceModel) {
        final TaggingResolver symTab = SymtabCreator.createSymTab("src/test/resources", "src/main/resources",
                Constants.SYNTHESIZED_COMPONENTS_ROOT);
        new ExecutionSemantics(symTab, pipelineReferenceModel).addExecutionSemantics();
        return pipelineReferenceModel;
    }

    private String getNetworkName(EMAComponentInstanceSymbol network) {
        String fullName = network.getFullName();
        int packageIndex = fullName.indexOf('.');
        return fullName.substring(packageIndex + 1).replace(".", "_");
    }

    /**
     * Returns the parsed configuration for the instance, given the configuration file name. Configuration files are prioritized as follows:
     * <ul>
     *     <li>Instance specific configuration</li>
     *     <li>Component type specific configuration</li>
     *     <li>Parent/Global configuration</li>
     * </ul>
     * @param configurationName The name of the configuration file
     * @return The parsed configuration for the instance, or null if no configuration file was found
     * @throws IOException If the configuration file could not be parsed
     */
    protected ASTConfLangCompilationUnit getInstanceConfiguration(
            String pathToModelsDirectory, String rootModelName,
            String instanceName, String componentTypeName, String configurationName) throws IOException {
        String pathToConfiguration;
        String modelDirPath = Paths.get(pathToModelsDirectory, rootModelName).toString();
        Path instanceSpecificPath = Paths.get(modelDirPath + "_" + instanceName, configurationName);
        Path componentTypeSpecificPath = Paths.get(pathToModelsDirectory, componentTypeName, configurationName);
        Path parentPath = Paths.get(modelDirPath, configurationName);

        if (Files.exists(instanceSpecificPath)) {
            // Priority 1: instance specific configuration
            pathToConfiguration = instanceSpecificPath.toString();
        } else if (Files.exists(componentTypeSpecificPath)) {
            // Priority 2: component type specific configuration
            pathToConfiguration = componentTypeSpecificPath.toString();
        } else if (Files.exists(parentPath)) {
            // Priority 3: parent/global configuration
            pathToConfiguration = parentPath.toString();
        } else {
            return null;
        }

        return this.parseConfigurationFile(pathToConfiguration);
    }

    public ASTConfLangCompilationUnit parseConfigurationFile(final String pathToConfigurationFile) throws IOException {
        return new ConfigurationLanguageParser().parseModelIfExists(pathToConfigurationFile);
    }

    private void copyPythonScriptsToTarget() {
        Log.info("Adding Python scripts from steps and schema_apis folder to target.", AbstractWorkflow.class.getName());

        List<String> stepDirPyFiles = Arrays.asList("HDF5DataAccess.py", "MyEvaluations.py", "MySupervisedTrainer.py", "Utils.py");
        String stepsResourceDir = "experiments/steps/";
        String stepsTargetDir = "target/generated-sources/steps/";

        List<String> schemaDirPyFiles = Arrays.asList("Supervised_Schema_API.py");
        String schemaResourceDir = "experiments/schema_apis/";
        String schemaTargetDir = "target/generated-sources/schema_apis/";

        List<String> trackingDirPyFiles = Arrays.asList("RunTracker.py");
        String trackingResourceDir = "experiments/tracking/";
        String trackingTargetDir = "target/generated-sources/tracking/";

        this.copyAllFiles(stepsResourceDir, stepsTargetDir, stepDirPyFiles);
        this.copyAllFiles(schemaResourceDir, schemaTargetDir, schemaDirPyFiles);
        this.copyAllFiles(trackingResourceDir, trackingTargetDir, trackingDirPyFiles);
    }

    private void copyAllFiles(String resourceDir, String targetDir, List<String> fileNameList) {
        for (String fileName : fileNameList) {
            URL fileURL = getClass().getClassLoader().getResource(resourceDir + fileName);
            try {
                if (fileURL != null) {
                    FileUtils.copyURLToFile(fileURL, new File(targetDir + fileName));
                } else {
                    Log.error(String.format("File %s not found in resource directory %s", fileName, resourceDir));
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected void copySchemasToTarget() {
        Log.info("Adding schema files for configuration validation to target.", AbstractWorkflow.class.getName());
        List<String> schemaFiles = Arrays.asList(
                "Cleaning.scm",
                "DataImbalance.scm",
                "DataSplitting.scm",
                "EfficientNet.scm",
                "Environment.scm",
                "EvalMetric.scm",
                "EvaluationCriteria.scm",
                "General.scm",
                "HyperparameterOpt.scm",
                "Loss.scm",
                "NeuralArchitectureSearch.scm",
                "NoiseDistribution.scm",
                "Optimizer.scm",
                "ReplayMemory.scm",
                "Supervised.scm");
        this.copyAllFiles("schemas/", this.schemasTargetDir, schemaFiles);
    }

    /**
     * Returns a list of all emadl files representing the root model as well as its subcomponents
     * @param rootEMADLComponent The root emadl component
     * @param pathToRootModel The path to the root emadl model
     * @return A list of all emadl files representing the root model as well as its subcomponents
     */
    private List<File> getModelEmadlFiles(ASTEMACompilationUnit rootEMADLComponent, String pathToRootModel){
        List<File> emadlFiles = new ArrayList<>();
        emadlFiles.add(new File(pathToRootModel));

        Path pathToRootModelDir = Paths.get(pathToRootModel).getParent();
        for(ASTSubComponent subComponent : rootEMADLComponent.getComponent().getSubComponents()) {
            if(subComponent.getType() instanceof ASTSimpleReferenceType) {
                ASTSimpleReferenceType type = (ASTSimpleReferenceType) subComponent.getType();

                Path emadlPath = Paths.get("");
                int nameListSize = type.getNameList().size();

                // Check if a package is specified. If not, the subcomponent is in the same package as the root model
                if(nameListSize > 1) {
                    // If pathToRootModelDir is absolute, we need to add the root to the path manually since subpath() returns a relative path
                    if (pathToRootModelDir.isAbsolute())
                        emadlPath = pathToRootModelDir.getRoot();
                    emadlPath = emadlPath.resolve(
                            pathToRootModelDir.subpath(0, pathToRootModelDir.getNameCount() - rootEMADLComponent.getPackageList().size()));

                    // Add all packages and finally the emadl file name
                    for (int i = 0; i < nameListSize - 1; i++) {
                        emadlPath = emadlPath.resolve(type.getName(i));
                    }
                    emadlPath = emadlPath.resolve(type.getName(nameListSize - 1) + ".emadl");
                } else {
                    emadlPath = pathToRootModelDir.resolve(type.getName(nameListSize - 1) + ".emadl");
                }

                File emadlFile = emadlPath.toFile();
                if(emadlFile.exists()) {
                    emadlFiles.add(emadlFile);
                } else {
                    Log.debug("Could not find emadl file for subcomponent " + type.getName(nameListSize-1), AbstractWorkflow.class.getName());
                }
            } else {
                Log.debug("Cannot find emadl file for subcomponents that are not SimpleTypeReferences", AbstractWorkflow.class.getName());
            }
        }
        return emadlFiles;
    }


}
