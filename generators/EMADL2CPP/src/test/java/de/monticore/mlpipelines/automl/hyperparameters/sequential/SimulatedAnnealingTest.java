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
import java.util.Map;

import static org.junit.Assert.assertNotEquals;

@RunWith(MockitoJUnitRunner.class)
public class SimulatedAnnealingTest extends TestCase {

    private ASTConfLangCompilationUnit initialHyperparams;

    private ASTConfLangCompilationUnit currBestHyperParams;

    private ASTConfLangCompilationUnit newCandidate;

    private ASTConfLangCompilationUnit searchSpace;

    private SimulatedAnnealing simulatedAnnealing1;

    private SimulatedAnnealing simulatedAnnealing2;

    private double evalValue = 0.8;

    private double currEvalValue = 0.7;

    private String metricType = "accuracy";

    @Before
    public void setup() throws IOException {
        Path modelPath = Paths.get("src/test/resources/models/automl/optimization_test");
        Path searchSpaceModelPath = Paths.get("src/test/resources/models/automl/searchspaces");
        String model = "Network.conf";

        ConfLangParser parser = new ConfLangParser();
        Path path = Paths.get(modelPath.toString(), model);
        Path searchSpacePath = Paths.get(searchSpaceModelPath.toString(), model);

        currBestHyperParams = parser.parse(path.toString()).get();
        searchSpace = parser.parse(searchSpacePath.toString()).get();

        this.simulatedAnnealing1 = new SimulatedAnnealing();

        initialHyperparams = simulatedAnnealing1.getInitialHyperparams(searchSpace);

        simulatedAnnealing1.setInitialTemperature(50.0);
        simulatedAnnealing1.executeOptimizationStep(initialHyperparams, searchSpace, evalValue, metricType);

        newCandidate = simulatedAnnealing1.getNewHyperparamsCandidate(searchSpace);

        simulatedAnnealing2 = new SimulatedAnnealing();
        simulatedAnnealing2.setInitialTemperature(50.0);
        simulatedAnnealing2.setCurrentIteration(1);

        simulatedAnnealing2.setCurrentHyperparameters(currBestHyperParams);
        simulatedAnnealing2.setCurrBestHyperparams(currBestHyperParams);
        simulatedAnnealing2.setCurrEvalMetric(currEvalValue);
        simulatedAnnealing2.setCurrBestEvalMetric(currEvalValue);

        simulatedAnnealing2.executeOptimizationStep(initialHyperparams, searchSpace, evalValue, metricType);
    }

    @Test
    public void testGetInitialNumEpoch() {
        int numEpoch = (int) ASTConfLangCompilationUnitHandler.getValueByKey(initialHyperparams, "num_epoch");
        Map<String, Object> numEpochRange = (Map<String, Object>) ASTConfLangCompilationUnitHandler.getValueByKey(searchSpace, "num_epoch");

        int lower = (int) numEpochRange.get("lower");
        int upper = (int) numEpochRange.get("upper");

        assertTrue(lower <= numEpoch);
        assertTrue(numEpoch <= upper);
    }

    @Test
    public void testGetInitialBatchSize() {
        int batchSize = (int) ASTConfLangCompilationUnitHandler.getValueByKey(initialHyperparams, "batch_size");
        Map<String, Object> batchSizeRange = (Map<String, Object>) ASTConfLangCompilationUnitHandler.getValueByKey(searchSpace, "batch_size");

        int lower = (int) batchSizeRange.get("lower");
        int upper = (int) batchSizeRange.get("upper");

        assertTrue(lower <= batchSize);
        assertTrue(batchSize <= upper);
    }

    @Test
    public void testGetInitialOptimizer() {
        Map<String, Object> optimizerMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(initialHyperparams, "optimizer");

        String optimizer = (String) optimizerMap.get("optimizer");
        Map<String, Object> nestedMap = (Map<String, Object>) optimizerMap.get("nestedMap");

        double learningRate = (double) nestedMap.get("learning_rate");
        int stepSize = (int) nestedMap.get("step_size");
        double weightDecay = (double) nestedMap.get("weight_decay");

        Map<String, Object> optimizerRangeMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(searchSpace, "optimizer");
        Map<String, Object> nestedRangeMap = (Map<String, Object>) optimizerRangeMap.get("nestedMap");

        Map<String, Object> learningRateRangeMap = (Map<String, Object>) nestedRangeMap.get("learning_rate");
        double learningRateLower = (double) learningRateRangeMap.get("lower");
        double learningRateUpper = (double) learningRateRangeMap.get("upper");

        Map<String, Object> stepSizeRangeMap = (Map<String, Object>) nestedRangeMap.get("step_size");
        int stepSizeLower = (int) stepSizeRangeMap.get("lower");
        int stepSizeUpper = (int) stepSizeRangeMap.get("upper");

        Map<String, Object> weightDecayRangeMap = (Map<String, Object>) nestedRangeMap.get("weight_decay");
        double weightDecayLower = (double) weightDecayRangeMap.get("lower");
        double weightDecayUpper = (double) weightDecayRangeMap.get("upper");

        assertEquals(optimizer, "adam");

        assertTrue(learningRateLower <= learningRate);
        assertTrue(learningRate <= learningRateUpper);

        assertTrue(stepSizeLower <= stepSize);
        assertTrue(stepSize <= stepSizeUpper);

        assertTrue(weightDecayLower <= weightDecay);
        assertTrue(weightDecay <= weightDecayUpper);
    }

    @Test
    public void testNewNumEpoch() {
        int numEpochBefore = (int) ASTConfLangCompilationUnitHandler.getValueByKey(initialHyperparams, "num_epoch");
        int newNumEpoch = (int) ASTConfLangCompilationUnitHandler.getValueByKey(newCandidate, "num_epoch");
        Map<String, Object> numEpochRange = (Map<String, Object>) ASTConfLangCompilationUnitHandler.getValueByKey(searchSpace, "num_epoch");

        int lower = (int) numEpochRange.get("lower");
        int upper = (int) numEpochRange.get("upper");
        int stepSize = (int) numEpochRange.get("step_size");

        int difference = Math.abs(newNumEpoch - numEpochBefore);

        assertTrue(lower <= newNumEpoch);
        assertTrue(newNumEpoch <= upper);
        assertTrue(difference <= stepSize);
    }

    @Test
    public void testNewBatchSize() {
        int newBatchSize = (int) ASTConfLangCompilationUnitHandler.getValueByKey(newCandidate, "batch_size");
        Map<String, Object> batchSizeRange = (Map<String, Object>) ASTConfLangCompilationUnitHandler.getValueByKey(searchSpace, "batch_size");

        int lower = (int) batchSizeRange.get("lower");
        int upper = (int) batchSizeRange.get("upper");

        assertTrue(lower <= newBatchSize);
        assertTrue(newBatchSize <= upper);
    }

    @Test
    public void testNewOptimizer() {
        Map<String, Object> optimizerMapBefore = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(initialHyperparams, "optimizer");
        Map<String, Object> newOptimizerMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(newCandidate, "optimizer");

        String optimizerBefore = (String) optimizerMapBefore.get("optimizer");
        String newOptimizer = (String) newOptimizerMap.get("optimizer");

        Map<String, Object> nestedMapBefore = (Map<String, Object>) optimizerMapBefore.get("nestedMap");
        Map<String, Object> newNestedMap = (Map<String, Object>) newOptimizerMap.get("nestedMap");

        double learningRateBefore = (double) nestedMapBefore.get("learning_rate");
        int stepSizeBefore = (int) nestedMapBefore.get("step_size");
        double weightDecayBefore = (double) nestedMapBefore.get("weight_decay");
        double newLearningRate = (double) newNestedMap.get("learning_rate");
        int newStepSize = (int) newNestedMap.get("step_size");
        double newWeightDecay = (double) newNestedMap.get("weight_decay");

        double learningRateDiff = Math.abs(newLearningRate - learningRateBefore);
        int stepSizeDiff = Math.abs(newStepSize - stepSizeBefore);
        double weightDecayDiff = Math.abs(newWeightDecay - weightDecayBefore);

        Map<String, Object> optimizerRangeMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(searchSpace, "optimizer");
        Map<String, Object> nestedRangeMap = (Map<String, Object>) optimizerRangeMap.get("nestedMap");

        Map<String, Object> learningRateRangeMap = (Map<String, Object>) nestedRangeMap.get("learning_rate");
        double learningRateLower = (double) learningRateRangeMap.get("lower");
        double learningRateUpper = (double) learningRateRangeMap.get("upper");
        double learningRateStepSize = (double) learningRateRangeMap.get("step_size");

        Map<String, Object> stepSizeRangeMap = (Map<String, Object>) nestedRangeMap.get("step_size");
        int stepSizeLower = (int) stepSizeRangeMap.get("lower");
        int stepSizeUpper = (int) stepSizeRangeMap.get("upper");
        int stepSizeStepSize = (int) stepSizeRangeMap.get("step_size");

        Map<String, Object> weightDecayRangeMap = (Map<String, Object>) nestedRangeMap.get("weight_decay");
        double weightDecayLower = (double) weightDecayRangeMap.get("lower");
        double weightDecayUpper = (double) weightDecayRangeMap.get("upper");
        double weightDecayStepSize = (double) weightDecayRangeMap.get("step_size");

        assertEquals(optimizerBefore, newOptimizer);

        assertTrue(learningRateLower <= newLearningRate);
        assertTrue(newLearningRate <= learningRateUpper);
        assertTrue(learningRateDiff <= learningRateStepSize);

        assertTrue(stepSizeLower <= newStepSize);
        assertTrue(newStepSize <= stepSizeUpper);
        assertTrue(stepSizeDiff <= stepSizeStepSize);

        assertTrue(weightDecayLower <= newWeightDecay);
        assertTrue(newWeightDecay <= weightDecayUpper);
        assertTrue(weightDecayDiff <= weightDecayStepSize);
    }

    @Test
    public void testFirstStepCurrBestHyperparams() {
        assertTrue(simulatedAnnealing1.getCurrBestHyperparams().deepEquals(initialHyperparams));
    }

    @Test
    public void testFirstStepCurrBestEvalMetric() {
        assertEquals(simulatedAnnealing1.getCurrBestEvalMetric(), evalValue);
    }

    @Test
    public void testFirstStepCurrentHyperparameters() {
        assertTrue(simulatedAnnealing1.getCurrentHyperparameters().deepEquals(initialHyperparams));
    }

    @Test
    public void testFirstStepCurrEvalMetric() {
        assertEquals(simulatedAnnealing1.getCurrEvalMetric(), evalValue);
    }

    @Test
    public void testFirstStepCurrentTemperature() {
        assertEquals(simulatedAnnealing1.getCurrentTemperature(), 25.0);
    }

    @Test
    public void testFirstStepNewHyperparamsCandidate() {
        Map<String, Boolean> initialKeys = ASTConfLangCompilationUnitHandler.getAllKeys(initialHyperparams);
        Map<String, Boolean> newKeys = ASTConfLangCompilationUnitHandler.getAllKeys(newCandidate);

        assertEquals(initialKeys, newKeys);
        assertNotEquals(initialHyperparams, newCandidate);
    }

    @Test
    public void testSecondStepCurrBestHyperparams() {
        assertTrue(simulatedAnnealing2.getCurrBestHyperparams().deepEquals(initialHyperparams));
    }

    @Test
    public void testSecondStepCurrBestEvalMetric() {
        assertEquals(simulatedAnnealing2.getCurrBestEvalMetric(), evalValue);
    }

    @Test
    public void testSecondStepCurrentHyperparameters() {
        assert(simulatedAnnealing2.getCurrentHyperparameters().deepEquals(currBestHyperParams) ||
                simulatedAnnealing2.getCurrentHyperparameters().deepEquals(initialHyperparams));
    }

    @Test
    public void testSecondStepCurrEvalMetric() {
        assert(simulatedAnnealing2.getCurrEvalMetric() == evalValue || simulatedAnnealing2.getCurrEvalMetric() == currEvalValue);
    }

    @Test
    public void testSecondStepCurrentTemperature() {
        assertEquals(simulatedAnnealing2.getCurrentTemperature(), (50.0/3.0));
    }

    @Test
    public void testSecondStepNewHyperparamsCandidate() {
        Map<String, Boolean> currentKeys = ASTConfLangCompilationUnitHandler.getAllKeys(simulatedAnnealing2.getCurrentHyperparameters());
        Map<String, Boolean> newKeys = ASTConfLangCompilationUnitHandler.getAllKeys(newCandidate);

        assertEquals(currentKeys, newKeys);
        assertNotEquals(simulatedAnnealing2.getCurrentHyperparameters(), newCandidate);
    }
}