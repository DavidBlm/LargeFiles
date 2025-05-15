package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.mlpipelines.automl.emadlprinter.ASTConfLangCompilationUnitPrinter;
import de.monticore.mlpipelines.automl.helper.ASTConfLangCompilationUnitHandler;
import de.monticore.mlpipelines.pipelines.Pipeline;
import de.monticore.mlpipelines.util.configuration_tracking.ConfigurationTrackingManager;
import de.se_rwth.commons.logging.Log;

import java.util.*;
import java.util.stream.Collectors;

public class SuccessiveHalvingAlgorithm extends SequentialAlgorithm {
    private int max_iter ;
    private int max_config ;
    private int eta;
    private int s_max;
    private double B ;
    ArrayList<Map<String, Object>> results = new ArrayList<>();
    List<Double> iterEvalValueList = new ArrayList<>();
    private double best_loss=Double.POSITIVE_INFINITY;
    private float best_accuracy;
    private double best_counter ;

    @Override
    public void executeOptimization( Pipeline pipeline, ASTConfLangCompilationUnit searchSpace,ASTConfLangCompilationUnit evaluationCriteria) {
        double criteria = (double) ASTConfLangCompilationUnitHandler.getValueByKey(evaluationCriteria, "acceptance_rate");
        int counter =0;
        Set<ASTConfLangCompilationUnit> nConfigurations = getFullSetOfNewHyperparamsCandidate(searchSpace, max_config);
        Double valLoss;
        Double evalValue;
        Float accuracy;
        String totalTime="";
        Map<ASTConfLangCompilationUnit, Double> map = new HashMap<>();
        outerloop:
        while(nConfigurations.size()>0){
             int n_configs = nConfigurations.size();
            Iterator<ASTConfLangCompilationUnit> iterator = nConfigurations.iterator();
            while (iterator.hasNext()) {
                Log.info(String.format("Iteration: %s", counter),SuccessiveHalvingAlgorithm.class.getName());
                Map<String,Object> result = new HashMap<>();
                counter++;
                ASTConfLangCompilationUnit currentHyperparams = iterator.next();
                //Override num_epoch with n_iterations as num_epoch is used as a budget for the training which means the number of iterations
                ASTConfLangCompilationUnitHandler.setValueForKey(currentHyperparams, "num_epoch", max_iter);
                if(pipeline != null) {
                    long startTime = System.currentTimeMillis();
                    pipeline.setTrainingConfiguration(currentHyperparams);
                    ConfigurationTrackingManager.executePipeline(pipeline,"HO: " + this.getClass().getSimpleName());
                    long endTime = System.currentTimeMillis();
                    totalTime = (endTime - startTime)/1000 + "s";
                }
                accuracy = pipeline.getTrainedAccuracy();
                evalValue = Double.valueOf(((Float) (pipeline.getTrainedAccuracy() / 100)).toString());
                valLoss  = 1-evalValue;

                result.put("iteration",counter);
                result.put("params",currentHyperparams);
                //result.put("iterations/epoch",n_iterations);
                result.put("accuracy",accuracy);
                result.put("loss",valLoss);
                result.put("time",totalTime);
                map.put(currentHyperparams,valLoss);
                this.results.add(result);
                iterEvalValueList.add((double) accuracy);
                if (valLoss < this.best_loss){
                    this.best_loss = valLoss;
                    this.best_accuracy= accuracy;
                    this.currBestHyperparams= currentHyperparams;
                }
                if (evalValue >= criteria) {
                    break outerloop;
                }
            }
            nConfigurations = topKconfigurations(map,n_configs,eta);
        }

        ASTConfLangCompilationUnitPrinter printer = new ASTConfLangCompilationUnitPrinter();
        Log.info(String.format("List of  hyperparameter configuration with matrices ... :\n%s", results),
                SuccessiveHalvingAlgorithm.class.getName());
        Log.info(String.format("Best hyperparameter configuration:\n%s", printer.prettyPrint(currBestHyperparams)),
                SuccessiveHalvingAlgorithm.class.getName());
        Log.info(String.format("Best Accuracy :%s", this.best_accuracy),
                SuccessiveHalvingAlgorithm.class.getName());
        Log.info(String.format("Best Loss :%s", this.best_loss),
                SuccessiveHalvingAlgorithm.class.getName());
        Log.info("Saving best hyperparameter configuration into a bestConfiguration.conf file", SuccessiveHalvingAlgorithm.class.getName());
        this.saveConfFile(currBestHyperparams, printer, pipeline.getNetworkName());
        Log.info("Saving eval value for each iteration into a evalValues.txt file", SuccessiveHalvingAlgorithm.class.getName());
        this.saveEvalValListAsFile(iterEvalValueList, pipeline.getNetworkName(), "evalValues.txt");
    }

    @Override
    public void executeOptimizationStep(ASTConfLangCompilationUnit hyperParams, ASTConfLangCompilationUnit searchSpace, Double evalValue, String metricType) {

    }

    private ASTConfLangCompilationUnit bestPerformingConfiguration(ArrayList<Map<String, Object>> results) {

        Collections.sort(results, Comparator.comparing(map -> (Comparable) map.get("loss")));
        Map<String, Object> firstMap = results.get(0);

        // Get the value for the specific key in the first HashMap
        currBestHyperparams = (ASTConfLangCompilationUnit) firstMap.get("params");
        this.best_loss = (double) firstMap.get("loss");
        this.best_accuracy = (float) firstMap.get("accuracy");
        return currBestHyperparams;


    }
    public Set<ASTConfLangCompilationUnit> topKconfigurations(Map<ASTConfLangCompilationUnit, Double> map, int n_configs, double eta) {
        Map<ASTConfLangCompilationUnit, Double> sortedMap = map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        int v = (int) (n_configs / eta);
        Set<ASTConfLangCompilationUnit> nConfigurations = new HashSet<>();

        nConfigurations= sortedMap.entrySet()
                .stream()
                .limit(v)
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());

        return nConfigurations ;
    }

    public Set<ASTConfLangCompilationUnit> getFullSetOfNewHyperparamsCandidate(ASTConfLangCompilationUnit searchSpace, int n) {
        Set<ASTConfLangCompilationUnit> nConfigurations = new HashSet<>();
        ASTConfLangCompilationUnit currentHyperparams;
        for(int i=0;i<n;i++){
            currentHyperparams= getNewHyperparamsCandidate(searchSpace);
            nConfigurations.add(currentHyperparams);
        }
        return nConfigurations;
    }

    @Override
    public ASTConfLangCompilationUnit getNewHyperparamsCandidate(ASTConfLangCompilationUnit searchSpace) {
        ASTConfLangCompilationUnit currentHyperparams= searchSpace.deepClone();

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
            //Object currentValue = ASTConfLangCompilationUnitHandler.getValueByKey(currentHyperparams, key);
            Object newValue;
            newValue = this.createValueFromRange(valueMap);
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
                newValue = this.createValueFromRange(nestedValueMap);
                currentHyperparams = ASTConfLangCompilationUnitHandler.setNestedValueForKeys(currentHyperparams, key, nestedKey, newValue);
            }
        }

        return currentHyperparams;
    }

    public void setMaxIter(int maxIter) { this.max_iter = maxIter;  }
    public void setEta(int eta) { this.eta = eta;   }
    public int getMaxIter() { return max_iter;  }
    public int getEta() {  return eta;   }
    public void setMaxConfig(int maxConfig) { this.max_config = maxConfig;  }
    public int getMaxConfig() { return max_config; }

}
