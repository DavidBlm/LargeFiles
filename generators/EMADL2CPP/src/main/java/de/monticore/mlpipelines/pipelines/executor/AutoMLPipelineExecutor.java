package de.monticore.mlpipelines.pipelines.executor;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.emadlprinter.ASTConfLangCompilationUnitPrinter;
import de.monticore.mlpipelines.automl.hyperparameters.AbstractHyperparameterAlgorithm;
import de.monticore.mlpipelines.automl.hyperparameters.HyperparamsOptAlgGenerator;
import de.monticore.mlpipelines.automl.trainalgorithms.NeuralArchitectureSearch;
import de.monticore.mlpipelines.automl.trainalgorithms.NeuralArchitectureSearchBuilder;
import de.monticore.mlpipelines.tracking.helper.ASTConfLangHelper;
import de.monticore.mlpipelines.tracking.tracker.MultiBackendTracker;
import de.monticore.mlpipelines.util.configuration_tracking.ConfigurationTrackingConf;
import de.monticore.mlpipelines.util.configuration_tracking.ConfigurationTrackingManager;
import de.monticore.symboltable.CommonScope;
import de.se_rwth.commons.logging.Log;
import java.util.Map;

public class AutoMLPipelineExecutor extends PipelineExecutor {

    private final NeuralArchitectureSearchBuilder neuralArchitectureSearchBuilder;
    private NeuralArchitectureSearch neuralArchitectureSearch;
    private ArchitectureSymbol finalArchitecture;

    private AbstractHyperparameterAlgorithm hyperparameterAlgorithm;


    protected ASTConfLangCompilationUnit searchSpace;

    public AutoMLPipelineExecutor() {
        neuralArchitectureSearchBuilder = new NeuralArchitectureSearchBuilder();
    }

    @Override
    public void execute() {
        MultiBackendTracker runTracker = montiAnnaContext.getTrackerFactory().createTracker();
        trainPipeline.setRunTracker(runTracker);

        for(int index = 0; index < networkExecutionConfigs.size(); index++) {
            this.applyBasicConfiguration(index);

            // Apply additional configurations
            Map<String, Object> configMap = this.networkExecutionConfigs.get(index);
            ASTConfLangCompilationUnit nasConf = (ASTConfLangCompilationUnit) configMap.get("nasConf");
            ASTConfLangCompilationUnit searchSpace = (ASTConfLangCompilationUnit) configMap.get("SearchSpace");
            ASTConfLangCompilationUnit hyperparamsOptConf = (ASTConfLangCompilationUnit) configMap.get("HyperparameterOpt");
            ASTConfLangCompilationUnit evaluationCriteria = (ASTConfLangCompilationUnit) configMap.get("EvaluationCriteria");

            // Log persistent data
            runTracker.addPersistentTag("Mode", "AutoML");
            runTracker.addPersistentTag("Root Model", montiAnnaContext.getRootModelName());
            runTracker.addPersistentTag("Network", trainPipeline.getNetworkName());
            runTracker.getArtifactHandler().setArtifacts(emadlFiles).setPath("emadl-Files").addExtension(".txt").logPersistent();

            ASTConfLangCompilationUnitPrinter printer = new ASTConfLangCompilationUnitPrinter();
            runTracker.getArtifactHandler().setPlaintext(printer.prettyPrint(nasConf)).setFileName("nasConf.txt").setPath("configuration").logPersistent();
            runTracker.getArtifactHandler().setPlaintext(printer.prettyPrint(searchSpace)).setFileName("searchSpace.txt").setPath("configuration").logPersistent();
            runTracker.getArtifactHandler().setPlaintext(printer.prettyPrint(hyperparamsOptConf)).setFileName("hyperparamsOptConf.txt").setPath("configuration").logPersistent();
            runTracker.getArtifactHandler().setPlaintext(printer.prettyPrint(evaluationCriteria)).setFileName("evaluationCriteria.txt").setPath("configuration").logPersistent();

            // Execute pipeline and clear persistent data for next network
            executeInstance(nasConf, searchSpace, hyperparamsOptConf, evaluationCriteria);
            runTracker.clearPersistentData();
        }
        runTracker.close();
    }

    private void executeInstance(
            ASTConfLangCompilationUnit nasConf,
            ASTConfLangCompilationUnit searchSpace,
            ASTConfLangCompilationUnit hyperparamsOptConf,
            ASTConfLangCompilationUnit evaluationCriteria
    ) {
        MultiBackendTracker runTracker = trainPipeline.getRunTracker(); // Same tracker as created in execute()

        setSearchSpace(trainPipeline.getTrainingConfiguration(), searchSpace);
        trainPipeline.setModelOutputDirectory(String.format("/model/%s/", trainPipeline.getNetworkName()));
        if (ConfigurationTrackingConf.isEnabled()) {
            ConfigurationTrackingManager.applyConfiguration(nasConf, searchSpace, hyperparamsOptConf, evaluationCriteria, trainPipeline, montiAnnaContext);
        }
        Log.info(String.format("Executing optimization for instance: %s", trainPipeline.getNetworkName()), AutoMLPipelineExecutor.class.getName());
        if (nasConf == null) {
            Log.info("Skip neural architecture search step since nas configuration not given",
                    AutoMLPipelineExecutor.class.getName());
        } else {
            ArchitectureSymbol originalArchitecture = getArchitectureSymbol();
            executeNeuralArchitectureSearch(originalArchitecture, trainPipeline.getTrainingConfiguration());
        }

        // NAS over, log the final architecture persistently
        runTracker.getArtifactHandler().setPlaintext(trainPipeline.getPrettyPrintedNetwork()).setFileName("network.txt").logPersistent();

        if ((hyperparamsOptConf == null) || (searchSpace == null)) {
            Log.info("Skip hyperparameter optimization step since configurations not given",
                    AutoMLPipelineExecutor.class.getName());
        } else {
            executeHyperparameterOptimization(hyperparamsOptConf, evaluationCriteria);
            trainPipeline.setTrainingConfiguration(hyperparameterAlgorithm.getCurrBestHyperparams());
        }

        // HO over, log the final hyperparams persistently
        runTracker.addPersistentParams(ASTConfLangHelper.getParametersFromConfiguration(trainPipeline.getTrainingConfiguration()));

        Log.info("Execute final training with optimized neural architecture and hyperparameters",
                AutoMLPipelineExecutor.class.getName());
        runTracker.startNewRun();
        runTracker.logTag("AutoML Stage", "Final Run");
        ConfigurationTrackingManager.executePipeline(trainPipeline, "Final Run");
        runTracker.endRun();
    }

    private ArchitectureSymbol getArchitectureSymbol() {
        CommonScope spannedScope = (CommonScope) this.trainPipeline.getNeuralNetwork().getSpannedScope();
        CommonScope subScope = (CommonScope) spannedScope.getSubScopes().get(0);
        return (ArchitectureSymbol) subScope.getSpanningSymbol().get();
    }

    private void executeNeuralArchitectureSearch(ArchitectureSymbol originalArchitecture, ASTConfLangCompilationUnit trainingConfiguration) {
        loadTrainAlgorithm(trainingConfiguration);
        finalArchitecture = neuralArchitectureSearch.execute(originalArchitecture);
    }

    public void loadTrainAlgorithm(ASTConfLangCompilationUnit trainingConfiguration) {
        neuralArchitectureSearchBuilder.setConfig(trainingConfiguration);
        neuralArchitectureSearchBuilder.setTrainingPipeline(trainPipeline);
        this.neuralArchitectureSearch = neuralArchitectureSearchBuilder.build();
    }

    private void executeHyperparameterOptimization(ASTConfLangCompilationUnit hyperparamsOptConf, ASTConfLangCompilationUnit evaluationCriteria) {
        hyperparameterAlgorithm = HyperparamsOptAlgGenerator.generateAlgorithm(hyperparamsOptConf,
                this.getSchemasTargetDir() + "HyperparameterOpt.scm");
        hyperparameterAlgorithm.executeOptimization(this.trainPipeline, this.searchSpace, evaluationCriteria);
    }

    private void setSearchSpace(ASTConfLangCompilationUnit trainingConfiguration, ASTConfLangCompilationUnit searchSpace) {
        if (searchSpace != null) {
            String trainConfigName = trainingConfiguration.getConfiguration().getName();
            searchSpace.getConfiguration().setName(trainConfigName);
            this.searchSpace = searchSpace;
        }
    }

    public ArchitectureSymbol getFinalArchitecture() {
        return finalArchitecture;
    }

}
