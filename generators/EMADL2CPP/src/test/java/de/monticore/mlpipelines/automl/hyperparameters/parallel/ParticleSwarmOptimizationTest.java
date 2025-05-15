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
public class ParticleSwarmOptimizationTest extends TestCase {

    private ParticleSwarmOptimization pso;

    private int populationSize = 5;

    private double c1 = 2;

    private double c2 = 2;

    private ASTConfLangCompilationUnit searchSpace;

    private List<ASTConfLangCompilationUnit> initialPopulation;

    List<ASTConfLangCompilationUnit> initialPopulationCopy;

    List<ASTConfLangCompilationUnit> newPopulation;

    private List<Double> evalValues = new ArrayList<>(Arrays.asList(0.2, 0.7, 0.9, 0.5, 0.65));

    private String metricType = "accuracy";

    @Before
    public void setup() throws IOException {
        Path searchSpaceModelPath = Paths.get("src/test/resources/models/automl/searchspaces");
        String model = "Network.conf";

        ConfLangParser parser = new ConfLangParser();
        Path searchSpacePath = Paths.get(searchSpaceModelPath.toString(), model);

        searchSpace = parser.parse(searchSpacePath.toString()).get();

        pso = new ParticleSwarmOptimization();

        pso.setPopulationSize(populationSize);
        pso.setC1(c1);
        pso.setC2(c2);

        initialPopulation = pso.initializePopulation(searchSpace);

        pso.executeOptimizationStep(initialPopulation, searchSpace, evalValues, metricType);

        initialPopulationCopy = this.deepCopyPopulation(initialPopulation);

        newPopulation = pso.getNewPopulation(searchSpace, metricType);
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
        assertEquals(pso.getCurrentPopulation(), initialPopulation);
    }

    @Test
    public void testEvalValues() {
        assertEquals(pso.getEvalValues(), evalValues);
    }

    @Test
    public void testPbestList() {
        assertEquals(pso.getPbestList(), initialPopulation);
    }

    @Test
    public void testPbestEvalList() {
        assertEquals(pso.getPbestEvalList(), evalValues);
    }

    @Test
    public void testBestConfig() {
        ASTConfLangCompilationUnit bestHyperparams = initialPopulationCopy.get(2);
        assertTrue(pso.getCurrBestHyperparams().deepEquals(bestHyperparams));
    }

    @Test
    public void testBestEvalValue() {
        assertEquals(pso.getCurrBestEvalMetric(), 0.9);
    }

    @Test
    public void testVelocityKeys() {
        List<Map<String, Object>> velocities = pso.getVelocities();
        for (int i=0; i < velocities.size(); i++) {
            Map<String, Object> velocityMap = velocities.get(i);
            ASTConfLangCompilationUnit config = initialPopulation.get(i);
            assertTrue(this.checkVelocityKeys(velocityMap, config));
        }
    }

    private boolean checkVelocityKeys(Map<String, Object> velocityMap, ASTConfLangCompilationUnit config) {
        Map<String, Boolean> configKeyMap = ASTConfLangCompilationUnitHandler.getAllKeys(config);
        Set<String> configKeys = configKeyMap.keySet();

        Set<String> velocityKeys = velocityMap.keySet();

        return configKeys.equals(velocityKeys);
    }

    @Test
    public void testGetNewPopulation() {
        assertEquals(newPopulation.size(), 5);

        assertFalse(initialPopulationCopy.equals(newPopulation));

        for (int i=0; i < newPopulation.size(); i++) {
            ASTConfLangCompilationUnit config = newPopulation.get(i);
            Map<String, Object> velocityMap = pso.getVelocities().get(i);

            assertTrue(this.allValuesInRange(config, searchSpace));
            assertTrue(this.keysCorrect(config, searchSpace));
            assertTrue(this.checkVelocityKeys(velocityMap, config));
        }
    }

    private List<ASTConfLangCompilationUnit> deepCopyPopulation(List<ASTConfLangCompilationUnit> population) {
        List<ASTConfLangCompilationUnit> populationCopy = new ArrayList<>();
        for (ASTConfLangCompilationUnit config : population) {
            populationCopy.add(config.deepClone());
        }
        return populationCopy;
    }

    @Test
    public void testExecuteSecondOptimizationStep() {
        //Pbest
        List<Double> newEvalValues = new ArrayList<>(Arrays.asList(0.1, 0.95, 0.92, 0.6, 0.7));
        pso.executeOptimizationStep(newPopulation, searchSpace, newEvalValues, metricType);

        assertEquals(pso.getCurrentPopulation(), newPopulation);
        assertEquals(pso.getEvalValues(), newEvalValues);

        List<ASTConfLangCompilationUnit> updatedPbestList = new ArrayList<>();
        updatedPbestList.add(initialPopulation.get(0));
        updatedPbestList.add(newPopulation.get(1));
        updatedPbestList.add(newPopulation.get(2));
        updatedPbestList.add(newPopulation.get(3));
        updatedPbestList.add(newPopulation.get(4));

        List<Double> updatedEvalValues = new ArrayList<>(Arrays.asList(0.2, 0.95, 0.92, 0.6, 0.7));

        assertEquals(pso.getPbestList(), updatedPbestList);
        assertEquals(pso.getPbestEvalList(), updatedEvalValues);

        ASTConfLangCompilationUnit bestHyperparams = newPopulation.get(1);
        assertTrue(pso.getCurrBestHyperparams().deepEquals(bestHyperparams));
        assertEquals(pso.getCurrBestEvalMetric(), 0.95);
    }

}