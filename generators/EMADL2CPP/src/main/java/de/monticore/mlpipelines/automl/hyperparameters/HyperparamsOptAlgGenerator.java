package de.monticore.mlpipelines.automl.hyperparameters;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.mlpipelines.automl.helper.ASTConfLangCompilationUnitHandler;
import de.monticore.mlpipelines.automl.helper.HyperHyperparameterDefaultValueExtractor;
import de.monticore.mlpipelines.automl.hyperparameters.parallel.GeneticAlgorithm;
import de.monticore.mlpipelines.automl.hyperparameters.parallel.ParticleSwarmOptimization;
import de.monticore.mlpipelines.automl.hyperparameters.sequential.*;
import de.se_rwth.commons.logging.Log;

import java.util.Map;

public class HyperparamsOptAlgGenerator {

    public static AbstractHyperparameterAlgorithm generateAlgorithm(ASTConfLangCompilationUnit hyperparamsOptConf,
                                                                    String schemaPath) {
        Map<String, Object> optimizerMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(hyperparamsOptConf, "optimizer");
        String optimizerName = (String) optimizerMap.get("optimizer");
        Map<String, Object> nestedMap = (Map<String, Object>) optimizerMap.get("nestedMap");
        Log.info(String.format("Use %s for hyperparameter optimization", optimizerName),
                HyperparamsOptAlgGenerator.class.getName());
        switch (optimizerName) {
            case "SA":
                return getSimulatedAnnealing(optimizerName, schemaPath, nestedMap);
            case "BO":
                return getBayesianOptimization(optimizerName, schemaPath, nestedMap);
            case "GA":
                return getGeneticAlgorithm(optimizerName, schemaPath, nestedMap);
            case "PSO":
                return getParticleSwarmOptimization(optimizerName, schemaPath, nestedMap);
            case "Hyperband":
                return getHyperbandAlgorithm(optimizerName, schemaPath, nestedMap);
            case "RS":
                return getRandomSearchAlgorithm(optimizerName, schemaPath, nestedMap);
            case "SH":
                return getSuccessiveHalvingAlgorithm(optimizerName, schemaPath, nestedMap);
            default:
                throw new IllegalArgumentException("Optimizer name in HyperparameterOpt.conf not valid.");
        }
    }

    private static Object getHyperHyperparams(String algName, String schemaPath, String attributeName,
                                              Map<String, Object> nestedMap) {
        Object value = nestedMap.get(attributeName);
        if (value == null) {
            value = HyperHyperparameterDefaultValueExtractor.getDefaultValue(schemaPath, algName, attributeName);
        }
        return value;
    }

    private static SimulatedAnnealing getSimulatedAnnealing(String algName, String schemaPath,
                                                            Map<String, Object> nestedMap) {
        SimulatedAnnealing sa = new SimulatedAnnealing();
        sa.setInitialTemperature((Double) getHyperHyperparams(algName, schemaPath, "initial_temperature",
                nestedMap));
        return sa;
    }

    private static BayesianOptimization getBayesianOptimization(String algName, String schemaPath,
                                                                Map<String, Object> nestedMap) {
        BayesianOptimization bo = new BayesianOptimization();
        bo.setNumRandomIter((Integer) getHyperHyperparams(algName, schemaPath, "num_random_iter",
                nestedMap));
        bo.setTradeOff((Double) getHyperHyperparams(algName, schemaPath, "tradeoff",
                nestedMap));
        return bo;
    }

    private static GeneticAlgorithm getGeneticAlgorithm(String algName, String schemaPath,
                                                        Map<String, Object> nestedMap) {
        GeneticAlgorithm ga = new GeneticAlgorithm();
        ga.setSelectionRate((Double) getHyperHyperparams(algName, schemaPath, "selection_rate",
                nestedMap));
        ga.setCrossoverConfig((Double) getHyperHyperparams(algName, schemaPath, "crossover_rate",
                nestedMap));
        ga.setMutationConfig((Double) getHyperHyperparams(algName, schemaPath, "mutation_rate",
                nestedMap));
        ga.setPopulationSize((Integer) getHyperHyperparams(algName, schemaPath, "population_size",
                nestedMap));
        return ga;
    }

    private static ParticleSwarmOptimization getParticleSwarmOptimization(String algName, String schemaPath,
                                                                          Map<String, Object> nestedMap) {
        ParticleSwarmOptimization pso = new ParticleSwarmOptimization();
        pso.setC1((Double) getHyperHyperparams(algName, schemaPath, "c1", nestedMap));
        pso.setC2((Double) getHyperHyperparams(algName, schemaPath, "c2", nestedMap));
        pso.setPopulationSize((Integer) getHyperHyperparams(algName, schemaPath, "population_size",
                nestedMap));
        return pso;
    }
    private static HyperbandAlgorithm getHyperbandAlgorithm(String algName, String schemaPath,
                                                            Map<String, Object> nestedMap) {
        HyperbandAlgorithm hyperband = new HyperbandAlgorithm();
        hyperband.setMaxIter((int) getHyperHyperparams(algName, schemaPath, "max_iter", nestedMap));
        hyperband.setEta((int) getHyperHyperparams(algName, schemaPath, "eta", nestedMap));
        hyperband.setSkipLast((int) getHyperHyperparams(algName, schemaPath, "skip_last", nestedMap));
        return hyperband;
    }

    private static RandomSearchAlgorithm getRandomSearchAlgorithm(String algName, String schemaPath,
                                                                  Map<String, Object> nestedMap) {
        RandomSearchAlgorithm rs = new RandomSearchAlgorithm();
        rs.setMaxIter((int) getHyperHyperparams(algName, schemaPath, "max_iter", nestedMap));
        return rs;
    }

    private static SuccessiveHalvingAlgorithm getSuccessiveHalvingAlgorithm(String algName, String schemaPath,
                                                                            Map<String, Object> nestedMap) {
        SuccessiveHalvingAlgorithm sh = new SuccessiveHalvingAlgorithm();
        sh.setMaxConfig((int) getHyperHyperparams(algName, schemaPath, "max_config", nestedMap));
        sh.setMaxIter((int) getHyperHyperparams(algName, schemaPath, "max_iter", nestedMap));
        sh.setEta((int) getHyperHyperparams(algName, schemaPath, "eta", nestedMap));
        return sh;
    }
}
