package de.monticore.mlpipelines.automl.hyperparameters.parallel;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.mlpipelines.automl.helper.ASTConfLangCompilationUnitHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GeneticAlgorithm extends ParallelAlgorithm {

    private double mutationConfig;

    private double crossoverConfig;

    private double selectionRate;

    public double getMutationConfig() {
        return mutationConfig;
    }

    public void setMutationConfig(double mutationConfig) {
        this.mutationConfig = mutationConfig;
    }

    public double getCrossoverConfig() {
        return crossoverConfig;
    }

    public void setCrossoverConfig(double crossoverConfig) {
        this.crossoverConfig = crossoverConfig;
    }

    public double getSelectionRate() {
        return selectionRate;
    }

    public void setSelectionRate(double selectionRate) {
        this.selectionRate = selectionRate;
    }

    private List<ASTConfLangCompilationUnit> executeSelection(List<ASTConfLangCompilationUnit> hyperParamsPopulation, List<Double> evalValues, String metricType) {
        int selectNum = (int) (hyperParamsPopulation.size() * this.selectionRate);
        selectNum = Math.max(selectNum, 2);

        List<ASTConfLangCompilationUnit> nBestConfigs = this.selectBestConfigs(hyperParamsPopulation, evalValues, selectNum, metricType);

        return nBestConfigs;
    }

    private List<ASTConfLangCompilationUnit> executeMutation(List<ASTConfLangCompilationUnit> hyperParams, ASTConfLangCompilationUnit searchSpace) {
        List<ASTConfLangCompilationUnit> mutatedConfigList = new ArrayList<>();

        Map<String, Boolean> keys = ASTConfLangCompilationUnitHandler.getAllKeys(searchSpace);
        for (ASTConfLangCompilationUnit config : hyperParams) {
            int listSizeBefore = mutatedConfigList.size();
            while (mutatedConfigList.size() != (listSizeBefore + 1)) {
                ASTConfLangCompilationUnit mutatedConfig = config.deepClone();
                for (Map.Entry<String, Boolean> keyEntry : keys.entrySet()) {
                    String key = keyEntry.getKey();
                    if (keyEntry.getValue()) {
                        // Treat nested key
                        mutatedConfig = this.executeMutationOnNestedKey(config, searchSpace, key, this.mutationConfig);
                    } else {
                        mutatedConfig = this.executeMutationOnKey(config, searchSpace, key, this.mutationConfig);
                    }
                }
                if (!this.checkHyperparamsInPopulation(mutatedConfig, mutatedConfigList)) {
                    mutatedConfigList.add(mutatedConfig);
                }
            }
        }
        return mutatedConfigList;
    }

    private ASTConfLangCompilationUnit executeMutationOnKey(ASTConfLangCompilationUnit config, ASTConfLangCompilationUnit searchSpace, String key, double mutationRate) {
        Object keyRange = ASTConfLangCompilationUnitHandler.getValueByKey(searchSpace, key);
        if ((keyRange instanceof Map) && (Math.random() < mutationRate)) {
            Map<String, Object> keyRangeMap = (Map<String, Object>) keyRange;
            Object newValue = this.createValueFromRange(keyRangeMap);
            config = ASTConfLangCompilationUnitHandler.setValueForKey(config, key, newValue);
        }
        return config;
    }

    private ASTConfLangCompilationUnit executeMutationOnNestedKey(ASTConfLangCompilationUnit config, ASTConfLangCompilationUnit searchSpace, String rootKey, double mutationRate) {
        Map<String, Object> rangeMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(searchSpace, rootKey);
        Map<String, Object> nestedMap = (Map<String, Object>) rangeMap.get("nestedMap");
        for (Map.Entry<String, Object> nestedEntry : nestedMap.entrySet()) {
            String nestedKey = nestedEntry.getKey();
            Object nestedValue = nestedEntry.getValue();
            if ((nestedValue instanceof Map) && (Math.random() < mutationRate)) {
                Object newNestedValue = this.createValueFromRange((Map<String, Object>) nestedValue);
                config = ASTConfLangCompilationUnitHandler.setNestedValueForKeys(config, rootKey, nestedKey, newNestedValue);
            }
        }
        return config;
    }

    private List<ASTConfLangCompilationUnit> selectBestConfigs(List<ASTConfLangCompilationUnit> population, List<Double> evalValues, int numConfigs, String metricType) {
        List<ASTConfLangCompilationUnit> nBestConfigs = new ArrayList<>();
        List<Double> sortedEvalValues = new ArrayList<>(evalValues);
        Collections.sort(sortedEvalValues);

        if (metricType.equals("accuracy")) {
            Collections.reverse(sortedEvalValues);
        }

        for (Double value : sortedEvalValues.subList(0, numConfigs)) {
            int index = evalValues.indexOf(value);
            ASTConfLangCompilationUnit config = population.get(index);
            nBestConfigs.add(config);
        }

        return nBestConfigs;
    }

    private List<ASTConfLangCompilationUnit> executeCrossover(List<ASTConfLangCompilationUnit> hyperParams) {
        List<ASTConfLangCompilationUnit> hyperParamsCopy = this.deepCopyConfigList(hyperParams);
        ASTConfLangCompilationUnit coCandidate1 = this.selectCrossoverCandidate(hyperParams, null);
        ASTConfLangCompilationUnit coCandidate2 = this.selectCrossoverCandidate(hyperParams, coCandidate1);

        while (hyperParamsCopy.size() < this.getPopulationSize()) {
            ASTConfLangCompilationUnit newConfig = crossoverConfigs(coCandidate1, coCandidate2);
            if ((!this.checkHyperparamsInPopulation(newConfig, hyperParamsCopy)) || (this.getPopulationSize() < 20)) {
                hyperParamsCopy.add(newConfig);
            }
        }

        return hyperParamsCopy;
    }

    private ASTConfLangCompilationUnit crossoverConfigs(ASTConfLangCompilationUnit parent1, ASTConfLangCompilationUnit parent2) {
        ASTConfLangCompilationUnit newConfig = parent1.deepClone();
        Map<String, Boolean> allKeys = ASTConfLangCompilationUnitHandler.getAllKeys(newConfig);

        for (Map.Entry<String, Boolean> keyEntry : allKeys.entrySet()) {
            String key = keyEntry.getKey();
            if (keyEntry.getValue()) {
                newConfig = this.crossoverNestedKeys(newConfig, parent2, key);
            } else {
                newConfig = this.crossoverKey(newConfig, parent2, key);
            }
        }

        return newConfig;
    }

    private ASTConfLangCompilationUnit crossoverKey(ASTConfLangCompilationUnit newConfig, ASTConfLangCompilationUnit parent2, String key) {
        if (Math.random() < this.crossoverConfig) {
            Object parentValue = ASTConfLangCompilationUnitHandler.getValueByKey(parent2, key);
            newConfig = ASTConfLangCompilationUnitHandler.setValueForKey(newConfig, key, parentValue);
        }
        return newConfig;
    }

    private ASTConfLangCompilationUnit crossoverNestedKeys(ASTConfLangCompilationUnit newConfig, ASTConfLangCompilationUnit parent2, String rootKey) {
        Map<String, Object> configMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(parent2, rootKey);
        Map<String, Object> nestedMap = (Map<String, Object>) configMap.get("nestedMap");
        for (Map.Entry<String, Object> nestedEntry : nestedMap.entrySet()) {
            if (Math.random() < this.crossoverConfig) {
                String nestedKey = nestedEntry.getKey();
                Object parentValue = nestedEntry.getValue();
                ASTConfLangCompilationUnitHandler.setNestedValueForKeys(newConfig, rootKey, nestedKey, parentValue);
            }
        }
        return newConfig;
    }

    private ASTConfLangCompilationUnit selectCrossoverCandidate(List<ASTConfLangCompilationUnit> hyperParams, ASTConfLangCompilationUnit excludeConfig) {
        List<ASTConfLangCompilationUnit> hyperParamsCopy = this.deepCopyConfigList(hyperParams);
        if (excludeConfig != null) {
            hyperParamsCopy = this.excludeConfigFromList(hyperParamsCopy, excludeConfig);
        }
        for (ASTConfLangCompilationUnit config : hyperParamsCopy) {
            if (Math.random() < (1.0/hyperParamsCopy.size())) {
                return config;
            }
        }
        return hyperParamsCopy.get(0);
    }

    private List<ASTConfLangCompilationUnit> deepCopyConfigList(List<ASTConfLangCompilationUnit> configList) {
        List<ASTConfLangCompilationUnit> configListCopy = new ArrayList<>();
        for (ASTConfLangCompilationUnit config : configList) {
            configListCopy.add(config.deepClone());
        }
        return configListCopy;
    }

    private List<ASTConfLangCompilationUnit> excludeConfigFromList(List<ASTConfLangCompilationUnit> configList, ASTConfLangCompilationUnit excludeConfig) {
        configList.removeIf(config -> config.deepEquals(excludeConfig));
        return configList;
    }

    @Override
    public void executeOptimizationStep(ASTConfLangCompilationUnit hyperParams, ASTConfLangCompilationUnit searchSpace, Double evalValue, String metricType) {

    }

    @Override
    public void executeOptimizationStep(List<ASTConfLangCompilationUnit> hyperParamsPopulation, ASTConfLangCompilationUnit searchSpace, List<Double> evalValues, String metricType) {
        this.setCurrentPopulation(hyperParamsPopulation);
        this.setEvalValues(evalValues);

        this.setBestConfigAndEval(metricType);

        this.executeIteration();
    }

    private void setBestConfigAndEval(String metricType) {
        List<Double> sortedEvalValues = new ArrayList<>(this.getEvalValues());
        Collections.sort(sortedEvalValues);

        if (metricType.equals("accuracy")) {
            Collections.reverse(sortedEvalValues);
        }

        double bestMetricVal = sortedEvalValues.get(0);

        int index = this.getEvalValues().indexOf(bestMetricVal);
        ASTConfLangCompilationUnit bestHyperparams = this.getCurrentPopulation().get(index).deepClone();

        this.setCurrBestHyperparams(bestHyperparams);
        this.setCurrBestEvalMetric(bestMetricVal);
    }

    @Override
    public ASTConfLangCompilationUnit getNewHyperparamsCandidate(ASTConfLangCompilationUnit searchSpace) {
        return null;
    }

    @Override
    public List<ASTConfLangCompilationUnit> getNewPopulation(ASTConfLangCompilationUnit searchSpace, String metricType) {
        List<ASTConfLangCompilationUnit> newPopulation = this.executeSelection(this.getCurrentPopulation(), this.getEvalValues(), metricType);
        newPopulation = this.executeCrossover(newPopulation);
        newPopulation = this.executeMutation(newPopulation, searchSpace);
        return newPopulation;
    }
}
