package de.monticore.mlpipelines.automl.helper;

import junit.framework.TestCase;

public class HyperHyperparameterDefaultValueExtractorTest  extends TestCase {

    String schemaPath = "src/test/resources/models/automl/schemas/HyperparameterOpt.scm";

    public void testSAInitialTemperature() {
        double initialTemperature = (double) HyperHyperparameterDefaultValueExtractor.getDefaultValue(schemaPath,
                "SA", "initial_temperature");
        assertEquals(initialTemperature, 50.0);
    }

    public void testBONumRandomIter() {
        int numRandomIter = (int) HyperHyperparameterDefaultValueExtractor.getDefaultValue(schemaPath,
                "BO", "num_random_iter");
        assertEquals(numRandomIter, 5);
    }

    public void testBOTradeoff() {
        double tradeoff = (double) HyperHyperparameterDefaultValueExtractor.getDefaultValue(schemaPath,
                "BO", "tradeoff");
        assertEquals(tradeoff, 0.01);
    }

    public void testGAPopulationSize() {
        int populationSize = (int) HyperHyperparameterDefaultValueExtractor.getDefaultValue(schemaPath,
                "GA", "population_size");
        assertEquals(populationSize, 10);
    }

    public void testGASelectionRate() {
        double selectionRate = (double) HyperHyperparameterDefaultValueExtractor.getDefaultValue(schemaPath,
                "GA", "selection_rate");
        assertEquals(selectionRate, 0.6);
    }

    public void testGACrossoverRate() {
        double crossoverRate = (double) HyperHyperparameterDefaultValueExtractor.getDefaultValue(schemaPath,
                "GA", "crossover_rate");
        assertEquals(crossoverRate, 0.5);
    }

    public void testGAMutationRate() {
        double mutationRate = (double) HyperHyperparameterDefaultValueExtractor.getDefaultValue(schemaPath,
                "GA", "mutation_rate");
        assertEquals(mutationRate, 0.3);
    }

    public void testPSOPopulationSize() {
        int populationSize = (int) HyperHyperparameterDefaultValueExtractor.getDefaultValue(schemaPath,
                "PSO", "population_size");
        assertEquals(populationSize, 10);
    }

    public void testPSOC1() {
        double c1 = (double) HyperHyperparameterDefaultValueExtractor.getDefaultValue(schemaPath,
                "PSO", "c1");
        assertEquals(c1, 2.0);
    }

    public void testPSOC2() {
        double c2 = (double) HyperHyperparameterDefaultValueExtractor.getDefaultValue(schemaPath,
                "PSO", "c2");
        assertEquals(c2, 2.0);
    }

    public void testHyperbandMaxIter() {
        int maxIter = (int) HyperHyperparameterDefaultValueExtractor.getDefaultValue(schemaPath,
                "Hyperband", "max_iter");
        assertEquals(maxIter, 9);
    }

    public void testHyperbandEta() {
        int eta = (int) HyperHyperparameterDefaultValueExtractor.getDefaultValue(schemaPath,
                "Hyperband", "eta");
        assertEquals(eta, 3);
    }

    public void testHyperbandSkipLast() {
        int skipLast = (int) HyperHyperparameterDefaultValueExtractor.getDefaultValue(schemaPath,
                "Hyperband", "skip_last");
        assertEquals(skipLast, 0);
    }

    public void testRSMaxIter() {
        int maxIter = (int) HyperHyperparameterDefaultValueExtractor.getDefaultValue(schemaPath,
                "RS", "max_iter");
        assertEquals(maxIter, 10);
    }

    public void testSHMaxConfig() {
        int maxConfig = (int) HyperHyperparameterDefaultValueExtractor.getDefaultValue(schemaPath,
                "SH", "max_config");
        assertEquals(maxConfig, 27);
    }

    public void testSHMaxIter() {
        int maxIter = (int) HyperHyperparameterDefaultValueExtractor.getDefaultValue(schemaPath,
                "SH", "max_iter");
        assertEquals(maxIter, 3);
    }

    public void testSHEta() {
        int eta = (int) HyperHyperparameterDefaultValueExtractor.getDefaultValue(schemaPath,
                "SH", "eta");
        assertEquals(eta, 3);
    }
}
