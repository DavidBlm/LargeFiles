package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.mlpipelines.automl.emadlprinter.ASTConfLangCompilationUnitPrinter;
import de.monticore.mlpipelines.automl.helper.ASTConfLangCompilationUnitHandler;
import de.monticore.mlpipelines.pipelines.Pipeline;
import de.monticore.mlpipelines.util.configuration_tracking.ConfigurationTrackingConf;
import de.monticore.mlpipelines.util.configuration_tracking.ConfigurationTrackingManager;
import de.se_rwth.commons.logging.Log;

import java.util.*;


public class RandomSearchAlgorithm extends SequentialAlgorithm {
    private int max_iter ;
    private float valLoss;
    private float accuracy;
    private float best_loss=Float.POSITIVE_INFINITY;
    private float best_accuracy;
    ArrayList<Map<String, Object>> results = new ArrayList<>();
    List<Double> iterEvalValueList = new ArrayList<>();
    protected ASTConfLangCompilationUnit bestTrainingConfig;


    @Override
    public void executeOptimization( Pipeline pipeline, ASTConfLangCompilationUnit searchSpace,ASTConfLangCompilationUnit evaluationCriteria) {
        int counter =0;

        for (int i = 0; i < max_iter; i++) {
            Log.info(String.format("Iteration: %s", i),RandomSearchAlgorithm.class.getName());
            ASTConfLangCompilationUnit currentHyperparams = getNewHyperparamsCandidate(searchSpace);
            Map<String,Object> result = new HashMap<>();
            long totalTime=0;
            counter++;
            if(pipeline != null) {
                long startTime = System.currentTimeMillis();
                pipeline.setTrainingConfiguration(currentHyperparams);
                ConfigurationTrackingManager.executePipeline(pipeline, "HO: " + this.getClass().getSimpleName());
                long endTime = System.currentTimeMillis();
                totalTime = endTime - startTime;
                if (ConfigurationTrackingConf.isEnabled()) {
                    accuracy = ConfigurationTrackingManager.getArtifact().getAccuracy();
                    valLoss = 1-accuracy;
                } else {
                    accuracy = pipeline.getTrainedAccuracy();
                    valLoss = 1-(Float.valueOf(((Float) (pipeline.getTrainedAccuracy() / 100)).toString()));
                }
            }
            result.put("iteration",counter);
            result.put("params",currentHyperparams);
            result.put("loss",valLoss);
            result.put("accuracy",accuracy);
            result.put("time",totalTime);
            this.results.add(result);
            iterEvalValueList.add((double) accuracy);
            if (valLoss < this.best_loss){
                this.best_loss = valLoss;
                this.best_accuracy= accuracy;
                this.currBestHyperparams= currentHyperparams;
            }

            //train model with the best hyperparameter configuration found
            pipeline.setTrainingConfiguration(currBestHyperparams);
            ConfigurationTrackingManager.executePipeline(pipeline,"HO: " + this.getClass().getSimpleName());
        }
        //this.bestTrainingConfig=bestPerformingConfiguration(results);
        ASTConfLangCompilationUnitPrinter printer = new ASTConfLangCompilationUnitPrinter();
        Log.info(String.format("List of  hyperparameter configuration with matrices ... :\n%s", results),
                RandomSearchAlgorithm.class.getName());
        Log.info(String.format("Best hyperparameter configuration:\n%s", printer.prettyPrint(currBestHyperparams)),
                RandomSearchAlgorithm.class.getName());
        Log.info(String.format("Best Accuracy :%s", this.best_accuracy),
                RandomSearchAlgorithm.class.getName());
        Log.info(String.format("Best Loss :%s", this.best_loss),
                RandomSearchAlgorithm.class.getName());
        Log.info("Saving best hyperparameter configuration into a bestConfiguration.conf file", RandomSearchAlgorithm.class.getName());
        this.saveConfFile(currBestHyperparams, printer, pipeline.getNetworkName());
        Log.info("Saving eval value for each iteration into a evalValues.txt file", RandomSearchAlgorithm.class.getName());
        this.saveEvalValListAsFile(iterEvalValueList, pipeline.getNetworkName(), "evalValues.txt");
    }

    @Override
    public void executeOptimizationStep(ASTConfLangCompilationUnit hyperParams, ASTConfLangCompilationUnit searchSpace, Double evalValue, String metricType) {

    }
    @Override
    public ASTConfLangCompilationUnit getNewHyperparamsCandidate(ASTConfLangCompilationUnit searchSpace) {
        ASTConfLangCompilationUnit currentHyperparams = searchSpace.deepClone();
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
            Object newValue;
            if (valueMap.containsKey("step_size")) {
                newValue = this.createValueFromStep(valueMap);
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
                Object newValue;
                Map<String, Object> nestedValueMap = (Map<String, Object>) nestedValue;
                if (nestedValueMap.containsKey("step_size")) {
                    newValue = this.createValueFromStep(nestedValueMap);
                } else {
                    newValue = this.createValueFromRange(nestedValueMap);
                }
                currentHyperparams = ASTConfLangCompilationUnitHandler.setNestedValueForKeys(currentHyperparams, key, nestedKey, newValue);
            }
        }

        return currentHyperparams;
    }

    protected Object createValueFromStep(Map<String, Object> rangeMap) {
        Object newValue;

        Object lower = rangeMap.get("lower");
        Object upper = rangeMap.get("upper");
        Object stepSize = rangeMap.get("step_size");

        if (this.isInteger(lower) && this.isInteger(upper) && this.isInteger(stepSize)) {
            int lowerInt = Integer.parseInt(lower.toString());
            int upperInt = Integer.parseInt(upper.toString());
            int stepSizeInt = Integer.parseInt(stepSize.toString());
            newValue = this.createIntFromStep(lowerInt, upperInt, stepSizeInt);
        } else {
            double lowerDouble = Double.parseDouble(lower.toString());
            double upperDouble = Double.parseDouble(upper.toString());
            double stepSizeDouble = Double.parseDouble(stepSize.toString());
            newValue = this.createDoubleFromStep(lowerDouble, upperDouble, stepSizeDouble);
        }

        return newValue;

    }

    private int createIntFromStep(int lower, int upper, int stepSize) {
        Random r = new Random();
        int range = ((upper - lower) / stepSize) + 1;
        int newValue = r.nextInt(range) * stepSize + lower;
        return newValue;
    }

    private double createDoubleFromStep(double lower, double upper, double stepSize) {
        Random r = new Random();
        int numDecimalPlaces = Math.max(0, (int)Math.ceil(-Math.log10(stepSize))); // number of decimal places
        String formatString = "%." + numDecimalPlaces + "f";
        double newValue = r.nextDouble() * (upper - lower) + lower;
        newValue = Math.round(newValue / stepSize) * stepSize;
        newValue = Double.parseDouble(String.format(formatString, newValue));
        return newValue ;
    }
    private Object keepValInRange(Object val, Object lower, Object upper) {
        Object valInRange;
        if (this.isInteger(val) && this.isInteger(lower) && this.isInteger(upper)) {
            int valInt = Integer.parseInt(val.toString());
            int lowerInt = Integer.parseInt(lower.toString());
            int upperInt = Integer.parseInt(upper.toString());
            valInRange = Math.max(valInt, lowerInt);
            valInRange = Math.min((Integer) valInRange, upperInt);
        } else {
            double valDouble = Double.parseDouble(val.toString());
            double lowerDouble = Double.parseDouble(lower.toString());
            double upperDouble = Double.parseDouble(upper.toString());
            valInRange = Math.max(valDouble, lowerDouble);
            valInRange = Math.min((double) valInRange, upperDouble);
        }
        return valInRange;
    }

    public void setMaxIter(int maxIter) { this.max_iter = maxIter;  }
    public int getMaxIter() { return max_iter;  }

}
