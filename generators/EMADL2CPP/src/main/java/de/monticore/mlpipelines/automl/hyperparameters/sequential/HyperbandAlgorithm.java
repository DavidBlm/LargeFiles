package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.mlpipelines.automl.emadlprinter.ASTConfLangCompilationUnitPrinter;
import de.monticore.mlpipelines.automl.helper.ASTConfLangCompilationUnitHandler;
import de.monticore.mlpipelines.pipelines.Pipeline;
import de.monticore.mlpipelines.util.configuration_tracking.ConfigurationTrackingConf;
import de.monticore.mlpipelines.util.configuration_tracking.ConfigurationTrackingManager;
import de.se_rwth.commons.logging.Log;

import java.util.*;
import java.util.stream.Collectors;

import static java.lang.Math.log;

public class HyperbandAlgorithm extends SequentialAlgorithm {
    private int max_iter ;
    private int eta;
    private int s_max;
    private double B ;
    ArrayList<Map<String, Object>> results = new ArrayList<>();
    ArrayList<Map<String, Object>> bestConfigInBracket = new ArrayList<>();
    List<Double> iterEvalValueList = new ArrayList<>();
    private double best_loss;
    private float best_accuracy;
    private double best_counter ;
    private int skipLast;
    @Override
    public void executeOptimization( Pipeline pipeline, ASTConfLangCompilationUnit searchSpace,ASTConfLangCompilationUnit evaluationCriteria) {
        double criteria = (double) ASTConfLangCompilationUnitHandler.getValueByKey(evaluationCriteria, "acceptance_rate");
        this.s_max = (int) logeta( this.max_iter,eta );
        this.B = ( this.s_max + 1 ) * this.max_iter ;
        this.best_counter = -1;
        int counter =0;
        outerloop:
        for (int s = this.s_max; s >=0; s--) {
            Log.info(String.format("Iteration: %s", counter),HyperbandAlgorithm.class.getName());
            // initial number of configurations
            int n = (int) Math.ceil( this.B / this.max_iter / ( s + 1 ) * Math.pow(this.eta,s ));
            //initial number of iterations per config
            int r = (int) (this.max_iter * Math.pow(this.eta,( -s )));
            Set<ASTConfLangCompilationUnit> nConfigurations = getFullSetOfNewHyperparamsCandidate(searchSpace, n);
            double valLoss=0;
            float accuracy=0;
            double evalValue=0;
            String totalTime="";
            Map<ASTConfLangCompilationUnit, Double> map = new HashMap<>();
            Map<String,Object> bestResult = new HashMap<>();
            for (int i = 0; i<=s-(skipLast); i++) {
                int n_configs = (int) (n * Math.pow(this.eta, ( -i )));
                int n_iterations = (int) (r * Math.pow(this.eta, i));
                Iterator<ASTConfLangCompilationUnit> iterator = nConfigurations.iterator();
                while (iterator.hasNext()) {
                    Map<String,Object> result = new HashMap<>();
                    counter++;
                    ASTConfLangCompilationUnit currentHyperparams = iterator.next();
                    //Override num_epoch with n_iterations as num_epoch is used as a budget for the training which means the number of iterations

                    ASTConfLangCompilationUnitHandler.setValueForKey(currentHyperparams, "num_epoch", n_iterations);
                    if(pipeline != null) {
                        long startTime = System.currentTimeMillis();
                        pipeline.setTrainingConfiguration(currentHyperparams);
                        ConfigurationTrackingManager.executePipeline(pipeline, "HO: " + this.getClass().getSimpleName());
                        long endTime = System.currentTimeMillis();
                        totalTime = (endTime - startTime)/1000 + "s";
                    }
                    if (ConfigurationTrackingConf.isEnabled()) {
                        accuracy = ConfigurationTrackingManager.getArtifact().getAccuracy();
                        evalValue = Double.valueOf(((Float) (accuracy / 100)).toString());
                        valLoss  = 1-evalValue;
                    } else {
                        accuracy = pipeline.getTrainedAccuracy();
                        evalValue = Double.valueOf(((Float) (pipeline.getTrainedAccuracy() / 100)).toString());
                        valLoss  = 1-evalValue;
                    }

                    result.put("counter",counter);
                    result.put("params",currentHyperparams);
                    result.put("iterations/epoch",n_iterations);
                    result.put("accuracy",accuracy);
                    result.put("loss",valLoss);
                    result.put("time",totalTime);
                    map.put(currentHyperparams,valLoss);
                    Log.info("Iteration", String.valueOf(counter));
                    this.results.add(result);
                    iterEvalValueList.add((double) accuracy);
                    bestResult = result;
                    if (evalValue >= criteria) {
                        break outerloop;
                    }
                }
                nConfigurations = topKconfigurations(map,n_configs,eta);
            }
            this.bestConfigInBracket.add(bestResult);
        }

        ASTConfLangCompilationUnitPrinter printer = new ASTConfLangCompilationUnitPrinter();
        Log.info(String.format("List of all hyperparameter configuration with matrices ... :\n%s", results),
                HyperbandAlgorithm.class.getName());
        Log.info(String.format("List of top hyperparameter configuration with matrices in each bracket... :\n%s", bestConfigInBracket),
                HyperbandAlgorithm.class.getName());
        this.currBestHyperparams = bestPerformingConfiguration(bestConfigInBracket);
        Log.info(String.format("Best hyperparameter configuration:\n%s", printer.prettyPrint(currBestHyperparams)),
                HyperbandAlgorithm.class.getName());
        Log.info(String.format("Best Accuracy:%s", this.best_accuracy),
                HyperbandAlgorithm.class.getName());
        Log.info(String.format("Best Loss:%s", this.best_loss),
                HyperbandAlgorithm.class.getName());
        Log.info("Saving best hyperparameter configuration into a bestConfiguration.conf file", HyperbandAlgorithm.class.getName());
        this.saveConfFile(currBestHyperparams, printer, pipeline.getNetworkName());
        Log.info("Saving eval value for each iteration into a evalValues.txt file", HyperbandAlgorithm.class.getName());
        this.saveEvalValListAsFile(iterEvalValueList, pipeline.getNetworkName(), "evalValues.txt");
        //train model with the best hyperparameter configuration found
        pipeline.setTrainingConfiguration(currBestHyperparams);
        ConfigurationTrackingManager.executePipeline(pipeline, "HO: " + this.getClass().getSimpleName());
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

    public double logeta(double x, double y) {
        return log(x) / log(y);
    }
    public int getConfigurationCount(int s) {
        int n = (int) Math.ceil( this.B / this.max_iter / ( s + 1 ) * Math.pow(this.eta,s ));
        return n;
    }
    public double getIterationCount(int s) {
        double r = this.max_iter * Math.pow(this.eta, -(s) );
        return r;
    }
    public void setMaxIter(int maxIter) { this.max_iter = maxIter;  }
    public void setEta(int eta) { this.eta = eta;   }
    public void setSkipLast(int skipLast){ this.skipLast = skipLast;}

    public int getMaxIter() { return max_iter;  }
    public int getEta() {  return eta;   }
    public int getSkipLast(){return skipLast ;}

}
