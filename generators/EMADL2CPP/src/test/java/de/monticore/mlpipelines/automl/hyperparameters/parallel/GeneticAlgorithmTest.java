package de.monticore.mlpipelines.automl.hyperparameters.parallel;

import conflang._ast.ASTConfLangCompilationUnit;
import conflang._parser.ConfLangParser;
import de.monticore.mlpipelines.automl.helper.ASTConfLangCompilationUnitHandler;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@RunWith(MockitoJUnitRunner.class)
public class GeneticAlgorithmTest extends TestCase {

    private GeneticAlgorithm geneticAlgorithm;

    private int populationSize = 5;

    private double mutationConfig = 0.1;

    private double crossoverConfig = 0.3;

    private double selectionRate = 0.4;

    private ASTConfLangCompilationUnit searchSpace;

    private List<ASTConfLangCompilationUnit> initialPopulation;

    private List<Double> evalValues = new ArrayList<>(Arrays.asList(0.2, 0.7, 0.9, 0.5, 0.65));

    private String metricType = "accuracy";

    @Before
    public void setup() throws IOException {
        Path searchSpaceModelPath = Paths.get("src/test/resources/models/automl/searchspaces");
        String model = "Network.conf";

        ConfLangParser parser = new ConfLangParser();
        Path searchSpacePath = Paths.get(searchSpaceModelPath.toString(), model);

        searchSpace = parser.parse(searchSpacePath.toString()).get();

        geneticAlgorithm = new GeneticAlgorithm();

        geneticAlgorithm.setPopulationSize(populationSize);
        geneticAlgorithm.setMutationConfig(mutationConfig);
        geneticAlgorithm.setCrossoverConfig(crossoverConfig);
        geneticAlgorithm.setSelectionRate(selectionRate);

        initialPopulation = geneticAlgorithm.initializePopulation(searchSpace);

        geneticAlgorithm.executeOptimizationStep(initialPopulation, searchSpace, evalValues, metricType);
    }

    @Test
    public void testInitialPopulation() {
        assertEquals(initialPopulation.size(), 5);

        for (ASTConfLangCompilationUnit config : initialPopulation) {
            assertTrue(this.allValuesInRange(config, searchSpace));
            assertTrue(this.keysCorrect(config, searchSpace));
        }
    }

    private boolean allValuesInRange(ASTConfLangCompilationUnit config, ASTConfLangCompilationUnit searchSpace) {
        boolean allValInRange = true;

        Map<String, Boolean> parameterKeys = ASTConfLangCompilationUnitHandler.getAllKeys(searchSpace);

        for (Map.Entry<String, Boolean> keyEntry : parameterKeys.entrySet()) {
            String key = keyEntry.getKey();
            if (keyEntry.getValue()) {
                allValInRange &= this.checkRangesForNestedVal(config, searchSpace, key);
            } else {
                allValInRange &= this.checkRangesForVal(config, searchSpace, key);
            }
        }

        return allValInRange;
    }

    private boolean checkRangesForVal(ASTConfLangCompilationUnit config, ASTConfLangCompilationUnit searchSpace, String key) {
        Map<String, Object> rangeMap = (Map<String, Object>) ASTConfLangCompilationUnitHandler.getValueByKey(searchSpace, key);
        Object configValObj = ASTConfLangCompilationUnitHandler.getValueByKey(config, key);
        double configVal = Double.parseDouble(configValObj.toString());

        double lower = Double.parseDouble(rangeMap.get("lower").toString());
        double upper = Double.parseDouble(rangeMap.get("upper").toString());

        boolean geqLower = (configVal >= lower);
        boolean leqUpper = (configVal <= upper);
        return geqLower && leqUpper;
    }

    private boolean checkRangesForNestedVal(ASTConfLangCompilationUnit config, ASTConfLangCompilationUnit searchSpace, String rootKey) {
        boolean allValInRange = true;

        Map<String, Object> searchSpaceMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(searchSpace, rootKey);
        Map<String, Object> nestedSearchSpaceMap = (Map<String, Object>) searchSpaceMap.get("nestedMap");

        Map<String, Object> configMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(config, rootKey);
        Map<String, Object> nestedConfigMap = (Map<String, Object>) configMap.get("nestedMap");

        for (Map.Entry<String, Object> nestedEntry : nestedSearchSpaceMap.entrySet()) {
            String nestedKey = nestedEntry.getKey();
            Map<String, Object> rangeMap = (Map<String, Object>) nestedEntry.getValue();
            double lower = Double.parseDouble(rangeMap.get("lower").toString());
            double upper = Double.parseDouble(rangeMap.get("upper").toString());

            Object nestedValObj = nestedConfigMap.get(nestedKey);
            double nestedValue = Double.parseDouble(nestedValObj.toString());

            boolean geqLower = (nestedValue >= lower);
            boolean leqUpper = (nestedValue <= upper);
            boolean isInRange = geqLower && leqUpper;

            allValInRange &= isInRange;
        }

        return allValInRange;
    }

    private boolean keysCorrect(ASTConfLangCompilationUnit config, ASTConfLangCompilationUnit searchSpace) {
        Map<String, Boolean> configKeysMap = ASTConfLangCompilationUnitHandler.getAllKeys(config);
        Map<String, Boolean> searchSpaceKeysMap = ASTConfLangCompilationUnitHandler.getAllKeys(searchSpace);

        boolean rootKeysEqual = configKeysMap.equals(searchSpaceKeysMap);
        boolean nestedKeysEqual = true;

        for (Map.Entry<String, Boolean> configKeysEntry : configKeysMap.entrySet()) {
            if (configKeysEntry.getValue()) {
                String rootKey = configKeysEntry.getKey();
                Map<String, Object> configMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(config, rootKey);
                Map<String, Object> nestedConfigMap = (Map<String, Object>) configMap.get("nestedMap");

                Map<String, Object> searchSpaceMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(searchSpace, rootKey);
                Map<String, Object> nestedSearchSpaceMap = (Map<String, Object>) searchSpaceMap.get("nestedMap");

                nestedKeysEqual = ((configMap.keySet().equals(searchSpaceMap.keySet())) && (nestedConfigMap.keySet().equals(nestedSearchSpaceMap.keySet())));
            }
        }
        return rootKeysEqual && nestedKeysEqual;
    }

    @Test
    public void testCurrentPopulation() {
        assertEquals(geneticAlgorithm.getCurrentPopulation(), initialPopulation);
    }

    @Test
    public void testEvalValues() {
        assertEquals(geneticAlgorithm.getEvalValues(), evalValues);
    }

    @Test
    public void testBestConfig() {
        ASTConfLangCompilationUnit bestHyperparams = initialPopulation.get(2);
        assertTrue(geneticAlgorithm.getCurrBestHyperparams().deepEquals(bestHyperparams));
    }

    @Test
    public void testBestEvalValue() {
        assertEquals(geneticAlgorithm.getCurrBestEvalMetric(), 0.9);
    }

    @Test
    public void testCurrentIteration() {
        assertEquals(geneticAlgorithm.getCurrentIteration(), 1);
    }

    @Test
    public void testGetNewPopulation() {
        List<ASTConfLangCompilationUnit> newPopulation = geneticAlgorithm.getNewPopulation(searchSpace, metricType);

        assertEquals(newPopulation.size(), 5);

        for (ASTConfLangCompilationUnit config : newPopulation) {
            assertTrue(this.allValuesInRange(config, searchSpace));
            assertTrue(this.keysCorrect(config, searchSpace));
        }
    }
}