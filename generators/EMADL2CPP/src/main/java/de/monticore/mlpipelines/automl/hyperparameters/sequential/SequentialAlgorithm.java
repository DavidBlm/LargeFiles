package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.mlpipelines.automl.emadlprinter.ASTConfLangCompilationUnitPrinter;
import de.monticore.mlpipelines.automl.helper.ASTConfLangCompilationUnitHandler;
import de.monticore.mlpipelines.automl.helper.ConfigurationValidationHandler;
import de.monticore.mlpipelines.automl.hyperparameters.AbstractHyperparameterAlgorithm;
import de.monticore.mlpipelines.pipelines.Pipeline;
import de.monticore.mlpipelines.tracking.helper.ASTConfLangHelper;
import de.monticore.mlpipelines.util.configuration_tracking.ConfigurationTrackingConf;
import de.monticore.mlpipelines.util.configuration_tracking.ConfigurationTrackingManager;
import de.se_rwth.commons.logging.Log;
import java.util.ArrayList;
import java.util.List;

public abstract class SequentialAlgorithm extends AbstractHyperparameterAlgorithm {

    private ASTConfLangCompilationUnit currentHyperparameters;

    public ASTConfLangCompilationUnit getCurrentHyperparameters() {
        return this.currentHyperparameters;
    }

    public void setCurrentHyperparameters(ASTConfLangCompilationUnit currentHyperparameters) {
        this.currentHyperparameters = currentHyperparameters;
    }

    protected boolean updateBest(double currValue, double newValue, String metricType) {
        if (metricType.equals("accuracy")) {
            return newValue > currValue;
        }
        else {
            return newValue < currValue;
        }
    }

    @Override
    public void executeOptimization(Pipeline pipeline, ASTConfLangCompilationUnit searchSpace, ASTConfLangCompilationUnit evaluationCriteria) {
        String metricType = (String) ASTConfLangCompilationUnitHandler.getValueByKey(evaluationCriteria, "metric");
        double criteria = (double) ASTConfLangCompilationUnitHandler.getValueByKey(evaluationCriteria, "acceptance_rate");
        int maxIterNum = (int) ASTConfLangCompilationUnitHandler.getValueByKey(evaluationCriteria, "max_iteration_number");

        List<Double> iterEvalValueList = new ArrayList<>();

        Log.info("Start with a random initial hyperparameter", SequentialAlgorithm.class.getName());
        ASTConfLangCompilationUnit trainingConfiguration = this.getInitialHyperparams(searchSpace);

        ASTConfLangCompilationUnitPrinter printer = new ASTConfLangCompilationUnitPrinter();

        while(this.getCurrentIteration() < maxIterNum) {
            ConfigurationValidationHandler.validateConfiguration(trainingConfiguration, pipeline.getSchemasTargetDir());
            pipeline.setTrainingConfiguration(trainingConfiguration);

            pipeline.getRunTracker().startNewRun();
            pipeline.getRunTracker().logTag("AutoML Stage", "HPO: " + this.getClass().getSimpleName());
            pipeline.getRunTracker().logParams(ASTConfLangHelper.getParametersFromConfiguration(trainingConfiguration));
            ConfigurationTrackingManager.executePipeline(pipeline, "HO: " + this.getClass().getSimpleName());
            pipeline.getRunTracker().endRun();

            double evalValue;
            if (ConfigurationTrackingConf.isEnabled()) {
                evalValue = ConfigurationTrackingManager.getArtifact().getAccuracy() / 100;
            } else {
                evalValue = Double.valueOf(((Float) (pipeline.getTrainedAccuracy() / 100)).toString());
            }
            iterEvalValueList.add(evalValue);

            Log.info(String.format("Current iteration: %s; Eval Value: %s", this.getCurrentIteration(), evalValue),
                    SequentialAlgorithm.class.getName());
            Log.info(String.format("Used hyperparameter configuration:\n%s", printer.prettyPrint(trainingConfiguration)),
                    SequentialAlgorithm.class.getName());

            Log.info("Execute optimization step and get new hyperparameter configuration",
                    SequentialAlgorithm.class.getName());
            this.executeOptimizationStep(trainingConfiguration, searchSpace, evalValue, metricType);
            trainingConfiguration = this.getNewHyperparamsCandidate(searchSpace);
            if (evalValue >= criteria) {
                break;
            }
        }

        ASTConfLangCompilationUnit bestTrainingConfig = this.getCurrBestHyperparams();
        pipeline.setTrainingConfiguration(bestTrainingConfig);
        double bestEvalValue = this.getCurrBestEvalMetric();

        Log.info(String.format("Best Eval Value: %s", bestEvalValue), SequentialAlgorithm.class.getName());
        Log.info(String.format("Best hyperparameter configuration:\n%s", printer.prettyPrint(bestTrainingConfig)),
                SequentialAlgorithm.class.getName());

        Log.info("Saving best hyperparameter configuration into a conf file", SequentialAlgorithm.class.getName());
        this.saveConfFile(bestTrainingConfig, printer, pipeline.getNetworkName());

        Log.info("Saving eval value for each iteration into a txt file", SequentialAlgorithm.class.getName());
        this.saveEvalValListAsFile(iterEvalValueList, pipeline.getNetworkName(), "evalValues.txt");
    }
}
