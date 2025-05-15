package de.monticore.mlpipelines.automl.hyperparameters.parallel;

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
import java.util.Collections;
import java.util.List;

public abstract class ParallelAlgorithm extends AbstractHyperparameterAlgorithm {

    private List<ASTConfLangCompilationUnit> currentPopulation;

    private int populationSize;

    private List<Double> evalValues;

    public List<ASTConfLangCompilationUnit> initializePopulation(ASTConfLangCompilationUnit searchSpace) {
        List<ASTConfLangCompilationUnit> hyperparamsPopulation = new ArrayList<>();

        while (hyperparamsPopulation.size() < this.populationSize) {
            ASTConfLangCompilationUnit hyperparams = this.getInitialHyperparams(searchSpace);
            if (!this.checkHyperparamsInPopulation(hyperparams, hyperparamsPopulation)) {
                hyperparamsPopulation.add(hyperparams);
            }
        }

        return hyperparamsPopulation;
    }

    protected boolean checkHyperparamsInPopulation(ASTConfLangCompilationUnit hyperparams, List<ASTConfLangCompilationUnit> population) {
        for (ASTConfLangCompilationUnit populationEntry : population) {
            if (populationEntry.deepEquals(hyperparams)) {
                return true;
            }
        }
        return false;
    }

    public List<ASTConfLangCompilationUnit> getCurrentPopulation() {
        return this.currentPopulation;
    }

    public void setCurrentPopulation(List<ASTConfLangCompilationUnit> currentPopulation) {
        this.currentPopulation = currentPopulation;
    }

    public List<Double> getEvalValues() {
        return evalValues;
    }

    public void setEvalValues(List<Double> evalValues) {
        this.evalValues = evalValues;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public abstract List<ASTConfLangCompilationUnit> getNewPopulation(ASTConfLangCompilationUnit searchSpace, String metricType);

    public abstract void executeOptimizationStep(List<ASTConfLangCompilationUnit> hyperParamsPopulation, ASTConfLangCompilationUnit searchSpace, List<Double> evalValues, String metricType);

    @Override
    public void executeOptimization(Pipeline pipeline, ASTConfLangCompilationUnit searchSpace, ASTConfLangCompilationUnit evaluationCriteria) {
        String metricType = (String) ASTConfLangCompilationUnitHandler.getValueByKey(evaluationCriteria, "metric");
        double criteria = (double) ASTConfLangCompilationUnitHandler.getValueByKey(evaluationCriteria, "acceptance_rate");
        int maxIterNum = (int) ASTConfLangCompilationUnitHandler.getValueByKey(evaluationCriteria, "max_iteration_number");

        List<Double> populationAvgEvalVals = new ArrayList<>();
        List<Double> populationBestEvalVals = new ArrayList<>();
        List<Double> populationWorstEvalVals = new ArrayList<>();

        Log.info("Start with a random initial population of hyperparams configs", ParallelAlgorithm.class.getName());
        List<ASTConfLangCompilationUnit> population = this.initializePopulation(searchSpace);

        ASTConfLangCompilationUnitPrinter printer = new ASTConfLangCompilationUnitPrinter();

        while(this.getCurrentIteration() < maxIterNum) {
            List<Double> evalValues = new ArrayList<>();
            for (ASTConfLangCompilationUnit trainingConfiguration : population) {
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
                evalValues.add(evalValue);
                Log.info(String.format("Current iteration: %s; Eval Value: %s", this.getCurrentIteration(), evalValue),
                        ParallelAlgorithm.class.getName());
                Log.info(String.format("Used hyperparameter configuration:\n%s", printer.prettyPrint(trainingConfiguration)),
                        ParallelAlgorithm.class.getName());

            }

            Log.info(String.format("Execution done for all configs in population of %s-th iteration", this.getCurrentIteration()),
                    ParallelAlgorithm.class.getName());

            double bestEvalOfPopulation = Collections.max(evalValues);
            double worstEvalOfPopulation = Collections.min(evalValues);
            double avgEvalOfPopulation = evalValues.stream().mapToDouble(d -> d).average().getAsDouble();

            populationBestEvalVals.add(bestEvalOfPopulation);
            populationWorstEvalVals.add(worstEvalOfPopulation);
            populationAvgEvalVals.add(avgEvalOfPopulation);

            Log.info(String.format("Best %s value of the population: %s", metricType, bestEvalOfPopulation),
                    ParallelAlgorithm.class.getName());
            Log.info(String.format("Worst %s value of the population: %s", metricType, worstEvalOfPopulation),
                    ParallelAlgorithm.class.getName());
            Log.info(String.format("Average %s value of the population: %s", metricType, avgEvalOfPopulation),
                    ParallelAlgorithm.class.getName());

            Log.info("Execute optimization step and get new population",
                    ParallelAlgorithm.class.getName());
            this.executeOptimizationStep(population, searchSpace, evalValues, metricType);
            population = this.getNewPopulation(searchSpace, metricType);
            if (bestEvalOfPopulation >= criteria) {
                break;
            }
        }

        ASTConfLangCompilationUnit bestTrainingConfig = this.getCurrBestHyperparams();
        pipeline.setTrainingConfiguration(bestTrainingConfig);
        double bestEvalValue = this.getCurrBestEvalMetric();

        Log.info(String.format("Best Eval Value: %s", bestEvalValue), ParallelAlgorithm.class.getName());
        Log.info(String.format("Best hyperparameter configuration:\n%s", printer.prettyPrint(bestTrainingConfig)),
                ParallelAlgorithm.class.getName());

        Log.info("Saving best hyperparameter configuration into a conf file", ParallelAlgorithm.class.getName());
        this.saveConfFile(bestTrainingConfig, printer, pipeline.getNetworkName());

        Log.info("Saving best, worst, average eval value for each iteration into a txt file", ParallelAlgorithm.class.getName());
        this.saveEvalValListAsFile(populationBestEvalVals, pipeline.getNetworkName(), "bestEvalValues.txt");
        this.saveEvalValListAsFile(populationWorstEvalVals, pipeline.getNetworkName(), "worstEvalValues.txt");
        this.saveEvalValListAsFile(populationAvgEvalVals, pipeline.getNetworkName(), "avgEvalValues.txt");
    }
}
