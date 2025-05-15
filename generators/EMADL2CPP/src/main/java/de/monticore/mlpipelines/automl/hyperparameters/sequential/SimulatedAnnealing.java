package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.mlpipelines.automl.helper.ASTConfLangCompilationUnitHandler;

import java.util.*;

public class SimulatedAnnealing extends SequentialAlgorithm {

    private double initialTemperature;

    private double currentTemperature;

    private double currEvalMetric;


    @Override
    public void executeOptimizationStep(ASTConfLangCompilationUnit hyperParams, ASTConfLangCompilationUnit searchSpace, Double evalValue, String metricType) {
        if (this.currentIteration == 0) {
            this.currBestHyperparams = hyperParams;
            this.currBestEvalMetric = evalValue;

            this.setCurrentHyperparameters(hyperParams);
            this.currEvalMetric = this.currBestEvalMetric;
        } else {
            if (this.updateBest(this.currBestEvalMetric, evalValue, metricType)) {
                this.currBestEvalMetric = evalValue;
                this.currBestHyperparams = hyperParams;
            }

            if (this.decideAcceptance(evalValue, metricType)) {
                this.setCurrentHyperparameters(hyperParams);
                this.currEvalMetric = evalValue;
            }
        }

        super.executeIteration();
        this.decreaseTemperature();
    }

    @Override
    public ASTConfLangCompilationUnit getNewHyperparamsCandidate(ASTConfLangCompilationUnit searchSpace) {
        ASTConfLangCompilationUnit currentHyperparams = this.getCurrentHyperparameters().deepClone();
        Map<String, Boolean> params = ASTConfLangCompilationUnitHandler.getAllKeys(searchSpace);

        for (Map.Entry<String, Boolean> entry : params.entrySet()) {
            String key = entry.getKey();
            Boolean isNested = entry.getValue();

            if (isNested) {
                currentHyperparams = this.updateNestedHyperparamsValue(searchSpace, currentHyperparams, key);
            } else {
                currentHyperparams = this.updateHyperparamsValue(searchSpace, currentHyperparams, key);
            }
        }

        return currentHyperparams;
    }

    private ASTConfLangCompilationUnit updateHyperparamsValue(ASTConfLangCompilationUnit searchSpace, ASTConfLangCompilationUnit currentHyperparams, String key) {
        Object searchSpaceValue = ASTConfLangCompilationUnitHandler.getValueByKey(searchSpace, key);
        if (searchSpaceValue instanceof Map) {
            Map<String, Object> valueMap = (Map<String, Object>) searchSpaceValue;
            Object currentValue = ASTConfLangCompilationUnitHandler.getValueByKey(currentHyperparams, key);
            Object newValue;
            if (valueMap.containsKey("step_size")) {
                newValue = this.createValueFromStep(valueMap, currentValue);
            } else {
                newValue = this.createValueFromRange(valueMap);
            }
            currentHyperparams = ASTConfLangCompilationUnitHandler.setValueForKey(currentHyperparams, key, newValue);
        }

        return currentHyperparams;
    }

    private ASTConfLangCompilationUnit updateNestedHyperparamsValue(ASTConfLangCompilationUnit searchSpace, ASTConfLangCompilationUnit currentHyperparams, String key) {
        Map<String, Object> configMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(searchSpace, key);
        Map<String, Object> nestedMap = (Map<String, Object>) configMap.get("nestedMap");
        for (Map.Entry<String, Object> nestedEntry : nestedMap.entrySet()) {
            String nestedKey = nestedEntry.getKey();
            Object nestedValue = nestedEntry.getValue();
            if (nestedValue instanceof Map) {
                Map<String, Object> currentValueMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(currentHyperparams, key);
                Map<String, Object> currentNestedMap = (Map<String, Object>) currentValueMap.get("nestedMap");
                Object currentValue = currentNestedMap.get(nestedKey);
                Object newValue;
                Map<String, Object> nestedValueMap = (Map<String, Object>) nestedValue;
                if (nestedValueMap.containsKey("step_size")) {
                    newValue = this.createValueFromStep(nestedValueMap, currentValue);
                } else {
                    newValue = this.createValueFromRange(nestedValueMap);
                }
                currentHyperparams = ASTConfLangCompilationUnitHandler.setNestedValueForKeys(currentHyperparams, key, nestedKey, newValue);
            }
        }

        return currentHyperparams;
    }

    private boolean decideAcceptance(double evaluationMetric, String metricType) {
        double diff = evaluationMetric - this.currEvalMetric;
        double criterion;
        if (metricType.equals("accuracy")) {
            criterion = Math.exp((diff) / this.currentTemperature);
            return (diff > 0) || (Math.random() < criterion);
        } else {
            criterion = Math.exp((-diff) / this.currentTemperature);
            return (diff < 0) || (Math.random() < criterion);
        }
    }

    private void decreaseTemperature() {
        this.currentTemperature = this.initialTemperature / (this.currentIteration + 1);
    }

    public double getInitialTemperature() {
        return initialTemperature;
    }

    public void setInitialTemperature(double initialTemperature) {
        this.initialTemperature = initialTemperature;
    }

    public double getCurrentTemperature() {
        return currentTemperature;
    }

    public double getCurrEvalMetric() {
        return currEvalMetric;
    }

    public void setCurrEvalMetric(double currEvalMetric) {
        this.currEvalMetric = currEvalMetric;
    }
}
