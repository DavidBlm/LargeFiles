package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import com.google.common.collect.Lists;
import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.mlpipelines.automl.helper.ASTConfLangCompilationUnitHandler;
import de.monticore.mlpipelines.automl.helper.MinMaxScaler;
import de.monticore.mlpipelines.automl.hyperparameters.sequential.regression.GaussianProcessRegression;
import org.apache.commons.math3.distribution.NormalDistribution;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class BayesianOptimization extends SequentialAlgorithm {

    private List<List<Double>> evaluatedConfigs;

    private List<Double> metricValues = new ArrayList<>();
    private int maxCandNumber = 50000;

    private int numRandomIter = 10;

    private double tradeOff = 0.01;

    private String metricType;

    private GaussianProcessRegression gpr = new GaussianProcessRegression();

    private List<Double> acquisition(double[][] samples) {
        List<Double> eiList = new ArrayList<>();

        for (int i=0; i < samples.length; i++) {
            double[][] xSample = new double[1][samples[0].length];
            xSample[0] = samples[i];
            Map<String, Object> predMap = gpr.predict(xSample);
            double mean = ((double[]) predMap.get("mean"))[0];
            double cov = ((double[][]) predMap.get("cov"))[0][0];
            eiList.add(this.calcExpectedImprovement(mean, cov, this.metricType));
        }

        return eiList;
    }

    private double calcExpectedImprovement(double mean, double cov, String metricType) {
        double std = Math.sqrt(cov);
        if (std == 0) {
            return 0;
        } else {
            NormalDistribution norm = new NormalDistribution();
            double imp = mean - this.currBestEvalMetric - this.tradeOff;
            double z = 0;
            if (std > 0) {
                z = imp / std;
            }
            double ei = imp * norm.cumulativeProbability(z) + std * norm.density(z);
            if (metricType.equals("accuracy")) {
                return ei;
            } else {
                return -ei;
            }
        }
    }

    private double[][] getConfigsArr() {
        int numConfigs = this.evaluatedConfigs.size();
        int numParams = this.evaluatedConfigs.get(0).size();
        double[][] configsArr = new double[numConfigs][numParams];
        for (int i=0; i < numConfigs; i++) {
            configsArr[i] = this.listToArr(this.evaluatedConfigs.get(i));
        }
        return configsArr;
    }

    private double[] getMetricValsArr() {
        return this.listToArr(this.metricValues);
    }

    private List<Double> compilationUnitToList(ASTConfLangCompilationUnit config) {
        List<Double> configList = new ArrayList<>();
        Map<String, Boolean> params = ASTConfLangCompilationUnitHandler.getAllKeys(config);
        SortedSet<String> sortedParams = new TreeSet<>(params.keySet());
        for (String key : sortedParams) {
            Boolean isNested = params.get(key);
            if (isNested) {
                Map<String, Object> configMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(config, key);
                Map<String, Object> nestedMap = (Map<String, Object>) configMap.get("nestedMap");
                SortedSet<String> sortedNestedParams = new TreeSet<>(nestedMap.keySet());
                for (String nestedKey : sortedNestedParams) {
                    double val = this.getDoubleVal(nestedMap.get(nestedKey));
                    configList.add(val);
                }
            } else {
                Object valObj = ASTConfLangCompilationUnitHandler.getValueByKey(config, key);
                double val = this.getDoubleVal(valObj);
                configList.add(val);
            }
        }
        return configList;
    }

    public double getDoubleVal(Object valObj) {
        double val;
        if (valObj instanceof String) {
            val = -1.0;
        } else {
            val = Double.parseDouble(valObj.toString());
        }
        return val;
    }

    private ASTConfLangCompilationUnit listToConfig(List<Double> configList, ASTConfLangCompilationUnit searchSpace) {
        ASTConfLangCompilationUnit config = searchSpace.deepClone();
        Map<String, Boolean> params = ASTConfLangCompilationUnitHandler.getAllKeys(config);

        int i = 0;

        SortedSet<String> sortedParams = new TreeSet<>(params.keySet());
        for (String key : sortedParams) {
            Boolean isNested = params.get(key);

            double val;

            if (isNested) {
                Map<String, Object> configMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(config, key);
                Map<String, Object> nestedMap = (Map<String, Object>) configMap.get("nestedMap");
                SortedSet<String> sortedNestedParams = new TreeSet<>(nestedMap.keySet());
                for (String nestedKey : sortedNestedParams) {
                    val = configList.get(i).doubleValue();
                    config = ASTConfLangCompilationUnitHandler.setNestedValueForKeys(config, key, nestedKey, val);
                    i++;
                }
            } else {
                val = configList.get(i).doubleValue();
                config = ASTConfLangCompilationUnitHandler.setValueForKey(config, key, val);
                i++;
            }
        }

        return config;
    }

    private List<List<Double>> buildCandidates(ASTConfLangCompilationUnit searchSpace, List<List<Double>> sampledList) {
        List<List<Double>> candidatesList;
        List<List<Double>> possibleValList = this.createPossibleValMap(searchSpace);
        int nPossibleConfigs = this.calcNumPossibilities(possibleValList);
        if (nPossibleConfigs <= this.maxCandNumber) {
            candidatesList = Lists.cartesianProduct(possibleValList);
            // candidateList to ArrayList
            candidatesList = deepCopyList(candidatesList);
            candidatesList = this.excludeSampledCandidates(candidatesList, sampledList);
        } else {
            candidatesList = new ArrayList<>();
            for (int i=0; i < this.maxCandNumber; i++) {
                List<Double> candidateConfig = this.createRandomCandidate(possibleValList);
                if (!candidatesList.contains(candidateConfig) && !sampledList.contains(candidateConfig)) {
                    candidatesList.add(candidateConfig);
                }
            }
        }
        return candidatesList;
    }

    private List<List<Double>> excludeSampledCandidates(List<List<Double>> candidatesList, List<List<Double>> sampledList) {
        List<List<Double>> candidateListCopy = this.deepCopyList(candidatesList);
        for (List<Double> candidate : candidateListCopy) {
            for (List<Double> sampled : sampledList) {
                if (sampled.equals(candidate)) {
                    candidatesList.remove(candidate);
                }
            }
        }
        return candidatesList;
    }

    private List<List<Double>> deepCopyList(List<List<Double>> list) {
        List<List<Double>> listCopy = new ArrayList<>();
        for (List<Double> listElem : list) {
            listCopy.add(listElem);
        }
        return listCopy;
    }

    private List<Double> createRandomCandidate(List<List<Double>> possibleValList) {
        List<Double> configCandidate = new ArrayList<>();
        Random rand = new Random();
        for (List<Double> valList : possibleValList) {
            double randValue = valList.get(rand.nextInt(valList.size()));
            configCandidate.add(randValue);
        }
        return configCandidate;
    }

    private int calcNumPossibilities(List<List<Double>> possibleValList) {
        int n = 1;
        for (List<Double> valList : possibleValList) {
            n *= valList.size();
        }
        return n;
    }

    private List<List<Double>> createPossibleValMap(ASTConfLangCompilationUnit searchSpace) {
        List<List<Double>> valList = new ArrayList<>();

        Map<String, Boolean> params = ASTConfLangCompilationUnitHandler.getAllKeys(searchSpace);

        SortedSet<String> sortedParams = new TreeSet<>(params.keySet());
        for (String key : sortedParams) {
            Boolean isNested = params.get(key);

            if (isNested) {
                valList.addAll(this.getAllPossibleNestedValues(searchSpace, key));
            } else {
                valList.add(this.getAllPossibleValues(searchSpace, key));
            }
        }

        return valList;
    }

    private List<Double> getAllPossibleValues(ASTConfLangCompilationUnit searchSpace, String key) {
        Object valObj = ASTConfLangCompilationUnitHandler.getValueByKey(searchSpace, key);
        return this.createValList(valObj);
    }

    private List<List<Double>> getAllPossibleNestedValues(ASTConfLangCompilationUnit searchSpace, String rootKey) {
        List<List<Double>> possibleNestedValList = new ArrayList<>();

        Map<String, Object> configMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(searchSpace, rootKey);
        Map<String, Object> nestedMap = (Map<String, Object>) configMap.get("nestedMap");

        SortedSet<String> sortedNestedParams = new TreeSet<>(nestedMap.keySet());

        for (String nestedKey : sortedNestedParams) {
            Object nestedValObj = nestedMap.get(nestedKey);
            possibleNestedValList.add(this.createValList(nestedValObj));
        }

        return possibleNestedValList;
    }

    private List<Double> createValList(Object valObj) {
        if (valObj instanceof Map) {
            Map<String, Object> valMap = (Map<String, Object>) valObj;
            Object lower = valMap.get("lower");
            Object upper = valMap.get("upper");
            Object stepSize;
            if (valMap.containsKey("step_size")) {
                stepSize = valMap.get("step_size");
            } else {
                stepSize = valMap.get("lower");
            }
            return this.createValsFromRange(lower, upper, stepSize);
        } else {
            if (valObj instanceof String) {
                return Arrays.asList(-1.0);
            } else {
                return Arrays.asList(Double.parseDouble(valObj.toString()));
            }
        }
    }

    private List<Double> createValsFromRange(Object lower, Object upper, Object stepSize) {
        BigDecimal lowerDecimal = new BigDecimal(lower.toString());
        BigDecimal upperDecimal = new BigDecimal(upper.toString());
        BigDecimal stepSizeDecimal = new BigDecimal(stepSize.toString());

        List<Double> valList = new ArrayList<>();
        while (lowerDecimal.doubleValue() <= upperDecimal.doubleValue()) {
            valList.add(lowerDecimal.doubleValue());
            lowerDecimal = lowerDecimal.add(stepSizeDecimal);
        }

        return valList;
    }

    public int getNumRandomIter() {
        return numRandomIter;
    }

    public void setNumRandomIter(int numRandomIter) {
        this.numRandomIter = numRandomIter;
    }

    public double getTradeOff() {
        return tradeOff;
    }

    public void setTradeOff(double tradeOff) {
        this.tradeOff = tradeOff;
    }

    @Override
    public ASTConfLangCompilationUnit getInitialHyperparams(ASTConfLangCompilationUnit searchSpace) {
        this.createInitialRandCandidates(searchSpace);

        List<Double> initConfigList = evaluatedConfigs.get(0);
        ASTConfLangCompilationUnit initialConfig = this.listToConfig(initConfigList, searchSpace);
        return initialConfig;
    }

    private void createInitialRandCandidates(ASTConfLangCompilationUnit searchSpace) {
        List<List<Double>> candidateList = this.buildCandidates(searchSpace, new ArrayList<>());
        List<Integer> randomPositions = new ArrayList<>();
        this.evaluatedConfigs = new ArrayList<>();

        // Select random positions for selecting from candidates
        while (randomPositions.size() < this.numRandomIter) {
            int randIndex = this.createRandInt(0, candidateList.size() - 1);
            while (randomPositions.contains(randIndex)) {
                randIndex = this.createRandInt(0, candidateList.size() - 1);
            }
            randomPositions.add(randIndex);
        }

        for (int index : randomPositions) {
            this.evaluatedConfigs.add(candidateList.get(index));
        }
    }

    @Override
    public void executeOptimizationStep(ASTConfLangCompilationUnit hyperParams, ASTConfLangCompilationUnit searchSpace, Double evalValue, String metricType) {
        if (this.currentIteration == 0) {
            this.setCurrBestHyperparams(hyperParams);
            this.setCurrBestEvalMetric(evalValue);
            this.metricType = metricType;
        } else {
            if (this.updateBest(this.currBestEvalMetric, evalValue, metricType)) {
                this.currBestEvalMetric = evalValue;
                this.currBestHyperparams = hyperParams;
            }
        }

        this.metricValues.add(evalValue);
        if (this.currentIteration >= this.numRandomIter) {
            this.evaluatedConfigs.add(this.compilationUnitToList(hyperParams));
        }

        this.executeIteration();
    }

    @Override
    public ASTConfLangCompilationUnit getNewHyperparamsCandidate(ASTConfLangCompilationUnit searchSpace) {
        if (this.currentIteration < this.numRandomIter) {
            List<Double> configList = this.evaluatedConfigs.get(this.currentIteration);
            return this.listToConfig(configList, searchSpace);
        } else {
            // Consider both observed and possible candidates for normalization
            List<List<Double>> samples = this.buildCandidates(searchSpace, this.evaluatedConfigs);
            List<List<Double>> consideredList = Stream.concat(this.evaluatedConfigs.stream(), samples.stream())
                    .collect(Collectors.toList());
            double[][] consideredArr = this.listTo2dArr(consideredList);
            double[][] normalizedArr = MinMaxScaler.normalizeArr(consideredArr, -1.0, 1.0);

            // Fit GPR on normalized observed data
            double[][] normalizedEvaluatedConfigArr = this.getSubArr(normalizedArr, 0, this.evaluatedConfigs.size());
            gpr.fit(normalizedEvaluatedConfigArr, this.getMetricValsArr());

            // Apply acquisition on normalized samples
            double[][] normalizedSamples = this.getSubArr(normalizedArr, this.evaluatedConfigs.size(), normalizedArr.length);
            List<Double> eiList = this.acquisition(normalizedSamples);

            // Return configuration with max EI
            Double maxEi = Collections.max(eiList);
            int maxId = eiList.indexOf(maxEi);
            List<Double> maxEiConfig = samples.get(maxId);
            return this.listToConfig(maxEiConfig, searchSpace);
        }
    }
}
