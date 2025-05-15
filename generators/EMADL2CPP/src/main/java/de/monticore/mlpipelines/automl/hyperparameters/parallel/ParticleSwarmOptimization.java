package de.monticore.mlpipelines.automl.hyperparameters.parallel;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.mlpipelines.automl.helper.ASTConfLangCompilationUnitHandler;

import java.util.*;

public class ParticleSwarmOptimization extends ParallelAlgorithm {

    private List<ASTConfLangCompilationUnit> pbestList;

    private List<Double> pbestEvalList;
    private List<Map<String, Object>> velocities;

    private double c1;

    private double c2;

    private void updatePbestList(String evalType) {
        for (int i=0; i < getPopulationSize(); i++) {
            double currEvalVal = this.pbestEvalList.get(i);
            double newEvalVal = this.getEvalValues().get(i);
            if (this.checkUpdateCondition(currEvalVal, newEvalVal, evalType)) {
                this.pbestEvalList.set(i, newEvalVal);
                ASTConfLangCompilationUnit pbest = getCurrentPopulation().get(i).deepClone();
                this.pbestList.set(i, pbest);
            }
        }
    }

    private boolean checkUpdateCondition(double currVal, double newVal, String metricType) {
        if (metricType.equals("accuracy")) {
            return currVal < newVal;
        } else {
            return currVal > newVal;
        }
    }

    private void updateGbest(String metricType) {
        double bestMetricValCandidate;
        if (metricType.equals("accuracy")) {
            bestMetricValCandidate = Collections.max(this.pbestEvalList);
        } else {
            bestMetricValCandidate = Collections.min(this.pbestEvalList);
        }

        if (this.checkUpdateCondition(this.currBestEvalMetric, bestMetricValCandidate, metricType)) {
            int bestMetricIndex = this.pbestEvalList.indexOf(bestMetricValCandidate);
            this.currBestHyperparams = this.pbestList.get(bestMetricIndex).deepClone();
            this.setCurrBestEvalMetric(bestMetricValCandidate);
        }
    }

    private void updateVelocities(ASTConfLangCompilationUnit searchSpace) {
        for (int i=0; i < this.getPopulationSize(); i++) {
            ASTConfLangCompilationUnit position = this.getCurrentPopulation().get(i);
            ASTConfLangCompilationUnit pbest = this.pbestList.get(i);
            Map<String, Object> velocity =  velocities.get(i);

            for (Map.Entry<String, Object> velocityEntry : velocity.entrySet()) {
                String key = velocityEntry.getKey();
                Object velocityValObj = velocityEntry.getValue();
                Object newVelocityValObj;
                if (velocityValObj instanceof Map) {
                    Map<String, Object> velocityMap = (Map<String, Object>) velocityValObj;
                    newVelocityValObj = this.getNewNestedVelocityMap(position, pbest, searchSpace, key, velocityMap);
                } else {
                    newVelocityValObj = this.getNewVelocityValue(position, pbest, searchSpace, key, velocityValObj);
                }
                velocity.put(key, newVelocityValObj);
            }
        }
    }

    private Object getNewVelocityValue(ASTConfLangCompilationUnit position, ASTConfLangCompilationUnit pbest, ASTConfLangCompilationUnit searchSpace, String key, Object velocityValObj) {
        Object posKeyVal = ASTConfLangCompilationUnitHandler.getValueByKey(position, key);
        Object pbestKeyVal = ASTConfLangCompilationUnitHandler.getValueByKey(pbest, key);
        Object gbestKeyVal = ASTConfLangCompilationUnitHandler.getValueByKey(this.currBestHyperparams, key);

        Object lower = this.getRangeProperty(searchSpace, key, "lower");
        Object upper = this.getRangeProperty(searchSpace, key, "upper");

        return this.calcNewVelocityVal(velocityValObj, posKeyVal, pbestKeyVal, gbestKeyVal, lower, upper);
    }

    private Map<String, Object> getNewNestedVelocityMap(ASTConfLangCompilationUnit position, ASTConfLangCompilationUnit pbest, ASTConfLangCompilationUnit searchSpace, String rootKey, Map<String, Object> velocityMap) {
        Map<String, Object> newNestedVelocityMap = new HashMap<>();

        Map<String, Object> posConfigMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(position, rootKey);
        Map<String, Object> posNestedMap = (Map<String, Object>) posConfigMap.get("nestedMap");

        Map<String, Object> pbestConfigMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(pbest, rootKey);
        Map<String, Object> pbestNestedMap = (Map<String, Object>) pbestConfigMap.get("nestedMap");

        Map<String, Object> gbestConfigMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(this.currBestHyperparams, rootKey);
        Map<String, Object> gbestNestedMap = (Map<String, Object>) gbestConfigMap.get("nestedMap");

        for (Map.Entry<String, Object> nestedPosEntry : posNestedMap.entrySet()) {
            String nestedKey = nestedPosEntry.getKey();
            Object nestedPosVal = nestedPosEntry.getValue();

            Object nestedVelocityVal = velocityMap.get(nestedKey);
            Object nestedPbestVal = pbestNestedMap.get(nestedKey);
            Object nestedGbestVal = gbestNestedMap.get(nestedKey);

            Object lower = this.getRangePropForNested(searchSpace, rootKey, nestedKey, "lower");
            Object upper = this.getRangePropForNested(searchSpace, rootKey, nestedKey, "upper");

            if (nestedVelocityVal != null) {
                Object newVelocityVal = this.calcNewVelocityVal(nestedVelocityVal, nestedPosVal, nestedPbestVal, nestedGbestVal, lower, upper);
                newNestedVelocityMap.put(nestedKey, newVelocityVal);
            }
        }

        return newNestedVelocityMap;
    }

    private Object calcNewVelocityVal(Object currVelocityVal, Object posKeyVal, Object pbestKeyVal, Object gbestKeyVal, Object lower, Object upper) {
        double r1 = Math.random();
        double r2 = Math.random();

        double cogComp = this.c1 * r1 * Double.parseDouble(this.subValObj(pbestKeyVal, posKeyVal, null, null).toString());
        double socComp = this.c2 * r2 * Double.parseDouble(this.subValObj(gbestKeyVal, posKeyVal, null, null).toString());

        double updateComp = cogComp + socComp;
        double newVelocityVal = Double.parseDouble(this.addValObj(currVelocityVal, updateComp, lower, upper).toString());

        if (this.isInteger(currVelocityVal) && this.isInteger(posKeyVal) && this.isInteger(pbestKeyVal) && this.isInteger(gbestKeyVal)) {
            return (int) newVelocityVal;
        } else {
            return newVelocityVal;
        }
    }

    private void updatePositions(ASTConfLangCompilationUnit searchSpace) {
        for (int i=0; i < this.getPopulationSize(); i++) {
            ASTConfLangCompilationUnit position = this.getCurrentPopulation().get(i);
            Map<String, Object> velocity = this.velocities.get(i);

            for (Map.Entry<String, Object> velocityEntry : velocity.entrySet()) {
                String key = velocityEntry.getKey();
                Object velocityValObj = velocityEntry.getValue();
                if (velocityValObj instanceof Map) {
                    Map<String, Object> velocityMap = (Map<String, Object>) velocityValObj;
                    position = this.updatePositionNestedKey(position, searchSpace, key, velocityMap);
                } else {
                    position = this.updatePositionKey(position, searchSpace, key, velocityValObj);
                }
            }
            this.getCurrentPopulation().set(i, position);
        }
    }

    private ASTConfLangCompilationUnit updatePositionKey(ASTConfLangCompilationUnit position, ASTConfLangCompilationUnit searchSpace, String key, Object velocityValObj) {
        Object posKeyValue = ASTConfLangCompilationUnitHandler.getValueByKey(position, key);

        Object lower = this.getRangeProperty(searchSpace, key, "lower");
        Object upper = this.getRangeProperty(searchSpace, key, "upper");

        Object newVal = this.addValObj(posKeyValue, velocityValObj, lower, upper);

        position = ASTConfLangCompilationUnitHandler.setValueForKey(position, key, newVal);

        return position;
    }

    private ASTConfLangCompilationUnit updatePositionNestedKey(ASTConfLangCompilationUnit position, ASTConfLangCompilationUnit searchSpace, String rootKey, Map<String, Object> velocityMap) {
        Map<String, Object> positionConfigMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(position, rootKey);
        Map<String, Object> positionNestedKeyMap = (Map<String, Object>) positionConfigMap.get("nestedMap");
        for (Map.Entry<String, Object> velocityEntry : velocityMap.entrySet()) {
            String nestedKey = velocityEntry.getKey();
            Object velocityValObj = velocityEntry.getValue();
            Object posNestedKeyVal = positionNestedKeyMap.get(nestedKey);

            Object lower = this.getRangePropForNested(searchSpace, rootKey, nestedKey, "lower");
            Object upper = this.getRangePropForNested(searchSpace, rootKey, nestedKey, "upper");

            Object newNestedVal = this.addValObj(posNestedKeyVal, velocityValObj, lower, upper);

            position = ASTConfLangCompilationUnitHandler.setNestedValueForKeys(position, rootKey, nestedKey, newNestedVal);
        }

        return position;
    }

    private void initVelocities(ASTConfLangCompilationUnit searchSpace) {
        List<Map<String, Object>> velocityList = new ArrayList<>();
        for (int i=0; i < this.getPopulationSize(); i++) {
            velocityList.add(this.initVelocityMap(searchSpace));
        }
        this.velocities = velocityList;
    }

    private Map<String, Object> initVelocityMap(ASTConfLangCompilationUnit searchSpace) {
        Map<String, Object> velocityMap = new HashMap<>();

        Map<String, Boolean> keysMap = ASTConfLangCompilationUnitHandler.getAllKeys(searchSpace);

        for (Map.Entry<String, Boolean> keyEntry : keysMap.entrySet()) {
            String key = keyEntry.getKey();
            if (keyEntry.getValue()) {
                Map<String, Object> nestedVelocityMap = this.getInitVelocitiesForNestedKey(searchSpace, key);
                velocityMap.put(key, nestedVelocityMap);
            } else {
                Object velocityVal = this.getInitVelocityForKey(searchSpace, key);
                if (! velocityVal.equals(Integer.MIN_VALUE)) {
                    velocityMap.put(key, velocityVal);
                }
            }
        }

        return velocityMap;
    }

    private Object getInitVelocityForKey(ASTConfLangCompilationUnit searchSpace, String key) {
        Object valObj = ASTConfLangCompilationUnitHandler.getValueByKey(searchSpace, key);
        if (valObj instanceof Map) {
            Map<String, Object> valMap = (Map<String, Object>) valObj;
            return this.getVelocityFromMap(valMap);
        }

        return Integer.MIN_VALUE;
    }
    private Map<String, Object> getInitVelocitiesForNestedKey(ASTConfLangCompilationUnit searchSpace, String rootKey) {
        Map<String, Object> nestedVelocitiesMap = new HashMap<>();

        Map<String, Object> configMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(searchSpace, rootKey);
        Map<String, Object> nestedMap = (Map<String, Object>) configMap.get("nestedMap");

        for (Map.Entry<String, Object> nestedEntry : nestedMap.entrySet()) {
            String nestedKey = nestedEntry.getKey();
            Object valObj = nestedEntry.getValue();
            if (valObj instanceof Map) {
                Map<String, Object> valMap = (Map<String, Object>) valObj;
                Object velocityVal = this.getVelocityFromMap(valMap);
                nestedVelocitiesMap.put(nestedKey, velocityVal);
            }
        }

        return nestedVelocitiesMap;
    }

    private Object getVelocityFromMap(Map<String, Object> valMap) {
        if (valMap.containsKey("step_size")) {
            Object stepSize = valMap.get("step_size");
            return this.getVelocityFromStep(stepSize);
        } else {
            Object lower = valMap.get("lower");
            Object upper = valMap.get("upper");
            return this.getVelocityFromRange(lower, upper);
        }
    }

    private Object getVelocityFromStep(Object stepSize) {
        if (this.isInteger(stepSize)) {
            int stepSizeInt = Integer.parseInt(stepSize.toString());
            return this.createRandInt(-stepSizeInt, stepSizeInt);
        } else {
            double stepSizeDouble = Double.parseDouble(stepSize.toString());
            return this.createRandDouble(-stepSizeDouble, stepSizeDouble);
        }
    }

    private Object getVelocityFromRange(Object lower, Object upper) {
        if (this.isInteger(lower) && this.isInteger(upper)) {
            int lowerInt = Integer.parseInt(lower.toString());
            return this.createRandInt(-lowerInt, lowerInt);
        } else {
            double lowerDouble = Double.parseDouble(lower.toString());
            return this.createRandDouble(-lowerDouble, lowerDouble);
        }
    }

    public double getC1() {
        return c1;
    }

    public void setC1(double c1) {
        this.c1 = c1;
    }

    public double getC2() {
        return c2;
    }

    public void setC2(double c2) {
        this.c2 = c2;
    }

    public List<ASTConfLangCompilationUnit> getPbestList() {
        return pbestList;
    }

    public List<Double> getPbestEvalList() {
        return pbestEvalList;
    }

    public List<Map<String, Object>> getVelocities() {
        return velocities;
    }

    @Override
    public void executeOptimizationStep(ASTConfLangCompilationUnit hyperParams, ASTConfLangCompilationUnit searchSpace, Double evalValue, String metricType) {

    }

    @Override
    public ASTConfLangCompilationUnit getNewHyperparamsCandidate(ASTConfLangCompilationUnit searchSpace) {
        return null;
    }

    @Override
    public List<ASTConfLangCompilationUnit> getNewPopulation(ASTConfLangCompilationUnit searchSpace, String metricType) {
        this.updateVelocities(searchSpace);
        this.updatePositions(searchSpace);

        return this.getCurrentPopulation();
    }

    @Override
    public void executeOptimizationStep(List<ASTConfLangCompilationUnit> hyperParamsPopulation, ASTConfLangCompilationUnit searchSpace, List<Double> evalValues, String metricType) {
        this.setCurrentPopulation(hyperParamsPopulation);
        this.setEvalValues(evalValues);

        if (this.getCurrentIteration() == 0) {
            this.initVelocities(searchSpace);
            this.pbestList = hyperParamsPopulation;
            this.pbestEvalList = evalValues;
        } else {
            this.updatePbestList(metricType);
        }

        this.updateGbest(metricType);

        this.executeIteration();
    }
}
