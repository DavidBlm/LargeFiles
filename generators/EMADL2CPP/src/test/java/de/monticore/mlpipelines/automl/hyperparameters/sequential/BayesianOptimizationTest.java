package de.monticore.mlpipelines.automl.hyperparameters.sequential;

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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotEquals;

@RunWith(MockitoJUnitRunner.class)
public class BayesianOptimizationTest extends TestCase {

    private ASTConfLangCompilationUnit initialHyperparams;

    private ASTConfLangCompilationUnit searchSpace;

    private ASTConfLangCompilationUnit newCandidate;

    private BayesianOptimization bayesianOptimization;

    private String metricType = "accuracy";

    private double evalValue = 0.8;

    @Before
    public void setup() throws IOException {
        Path searchSpaceModelPath = Paths.get("src/test/resources/models/automl/searchspaces");
        String model = "Network.conf";
        ConfLangParser parser = new ConfLangParser();
        Path searchSpacePath = Paths.get(searchSpaceModelPath.toString(), model);
        searchSpace = parser.parse(searchSpacePath.toString()).get();

        bayesianOptimization = new BayesianOptimization();
        bayesianOptimization.setNumRandomIter(5);

        initialHyperparams = bayesianOptimization.getInitialHyperparams(searchSpace);

        bayesianOptimization.executeOptimizationStep(initialHyperparams, searchSpace, evalValue, metricType);

        newCandidate = bayesianOptimization.getNewHyperparamsCandidate(searchSpace);
    }

    private boolean isInteger(Object numberObj) {
        return !numberObj.toString().contains(".");
    }

    private void testConfigParameter(ASTConfLangCompilationUnit config, String key, boolean nested) {
        if (nested) {
            Map<String, Object> configMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(config, key);
            this.testInitialNestedParameter(configMap, key);
        } else {
            Object paramValObj = ASTConfLangCompilationUnitHandler.getValueByKey(config, key);
            Map<String, Object> searchSpaceRange = (Map<String, Object>) ASTConfLangCompilationUnitHandler.getValueByKey(searchSpace, key);
            this.testParameter(paramValObj, searchSpaceRange);
        }
    }

    private void testParameter(Object paramValObj, Map<String, Object> paramRange) {
        if (isInteger(paramValObj)) {
            int intParam = (int) paramValObj;

            int lower = (int) paramRange.get("lower");
            int upper = (int) paramRange.get("upper");

            assertTrue(lower <= intParam);
            assertTrue(intParam <= upper);

            if (paramRange.containsKey("step_size")) {
                int stepSize = (int) paramRange.get("step_size");
                assertTrue(intParam % stepSize == 0);
            }
        } else {
            double doubleParam = (double) paramValObj;

            double lower = (double) paramRange.get("lower");
            double upper = (double) paramRange.get("upper");

            assertTrue(lower <= doubleParam);
            assertTrue(doubleParam <= upper);
        }

    }

    private void testInitialNestedParameter(Map<String, Object> configMap, String rootKey) {
        Map<String, Object> searchSpaceMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(searchSpace, rootKey);
        Map<String, Object> searchSpaceNestedMap = (Map<String, Object>) searchSpaceMap.get("nestedMap");

        String rootVal = (String) configMap.get(rootKey);
        assertEquals(rootVal, searchSpaceMap.get(rootKey).toString());


        Map<String, Object> nestedMap = (Map<String, Object>) configMap.get("nestedMap");

        for (Map.Entry<String, Object> nestedEntry : nestedMap.entrySet()) {
            String nestedKey = nestedEntry.getKey();
            Object nestedValObj = nestedEntry.getValue();
            Map<String, Object> rangeMap = (Map<String, Object>) searchSpaceNestedMap.get(nestedKey);
            this.testParameter(nestedValObj, rangeMap);
        }
    }

    @Test
    public void testInitialNumEpoch() {
        this.testConfigParameter(initialHyperparams, "num_epoch", false);
    }

    @Test
    public void testInitialBatchSize() {
        this.testConfigParameter(initialHyperparams, "batch_size", false);
    }

    @Test
    public void testInitialOptimizer() {
        this.testConfigParameter(initialHyperparams, "optimizer", true);
    }

    @Test
    public void testNewNumEpoch() {
        this.testConfigParameter(newCandidate, "num_epoch", false);
    }

    @Test
    public void testNewBatchSize() {
        this.testConfigParameter(newCandidate, "batch_size", false);
    }

    @Test
    public void testNewOptimizer() {
        this.testConfigParameter(newCandidate, "optimizer", true);
    }

    @Test
    public void testFirstStepCurrBestHyperparams() {
        assertTrue(bayesianOptimization.getCurrBestHyperparams().deepEquals(initialHyperparams));
    }

    @Test
    public void testFirstStepCurrBestEvalMetric() {
        assertEquals(bayesianOptimization.getCurrBestEvalMetric(), evalValue);
    }

    @Test
    public void testFirstStepNewHyperparamsCandidate() {
        Map<String, Boolean> initialKeys = ASTConfLangCompilationUnitHandler.getAllKeys(initialHyperparams);
        Map<String, Boolean> newKeys = ASTConfLangCompilationUnitHandler.getAllKeys(newCandidate);

        assertEquals(initialKeys, newKeys);
        assertNotEquals(initialHyperparams, newCandidate);
    }

    @Test
    public void testBOStep() {
        List<Double> evalValues = new ArrayList<>(Arrays.asList(0.2, 0.7, 0.6, 0.5, 0.9));
        for (double evalVal : evalValues) {
            bayesianOptimization.executeOptimizationStep(newCandidate, searchSpace, evalVal, metricType);
            newCandidate = bayesianOptimization.getNewHyperparamsCandidate(searchSpace);
        }

        Map<String, Boolean> newKeys = ASTConfLangCompilationUnitHandler.getAllKeys(newCandidate);
        for (Map.Entry<String, Boolean> keyEntry : newKeys.entrySet()) {
            String key = keyEntry.getKey();
            Boolean nested = keyEntry.getValue();
            this.testConfigParameter(newCandidate, key, nested);
        }

        assertFalse(bayesianOptimization.getCurrBestHyperparams().deepEquals(newCandidate));
        assertEquals(bayesianOptimization.getCurrBestEvalMetric(), 0.9);
    }
}