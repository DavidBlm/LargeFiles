package de.monticore.mlpipelines.automl.hyperparameters.sequential;

import conflang._ast.ASTConfLangCompilationUnit;
import conflang._parser.ConfLangParser;
import de.monticore.mlpipelines.automl.emadlprinter.ASTConfLangCompilationUnitPrinter;
import de.monticore.mlpipelines.automl.helper.ASTConfLangCompilationUnitHandler;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class RandomSearchAlgorithmTest extends TestCase {
    private ASTConfLangCompilationUnit searchSpace;
    private RandomSearchAlgorithm randomSearchAlgorithm;
    private ASTConfLangCompilationUnit newHyperparamcandidate;

    @Before
    public void setUp() throws Exception {
        Path modelPath = Paths.get("src/test/resources/models/automl/optimization_test");
        Path searchSpaceModelPath = Paths.get("src/test/resources/models/automl/searchspaces");
        String model = "Network.conf";

        ConfLangParser parser = new ConfLangParser();
        Path path = Paths.get(modelPath.toString(), model);
        Path searchSpacePath = Paths.get(searchSpaceModelPath.toString(), model);

        searchSpace = parser.parse(searchSpacePath.toString()).get();
        this.randomSearchAlgorithm = new RandomSearchAlgorithm();
        newHyperparamcandidate = randomSearchAlgorithm.getNewHyperparamsCandidate(searchSpace);


    }

    @Test
    public void testGetNewHyperparamsCandidate() {
        ASTConfLangCompilationUnit configuration = randomSearchAlgorithm.getNewHyperparamsCandidate(searchSpace);
        assertNotNull(configuration);
        ASTConfLangCompilationUnitPrinter printer = new ASTConfLangCompilationUnitPrinter();
        System.out.println(printer.prettyPrint(configuration));
    }
    @Test
    public void testGetRandomNumEpoch() {
        int numEpoch = (int) ASTConfLangCompilationUnitHandler.getValueByKey(newHyperparamcandidate, "num_epoch");
        Map<String, Object> numEpochRange = (Map<String, Object>) ASTConfLangCompilationUnitHandler.getValueByKey(searchSpace, "num_epoch");

        int lower = (int) numEpochRange.get("lower");
        int upper = (int) numEpochRange.get("upper");

        assertTrue(lower <= numEpoch);
        assertTrue(numEpoch <= upper);
    }

    @Test
    public void testGetRandomBatchSize() {
        int batchSize = (int) ASTConfLangCompilationUnitHandler.getValueByKey(newHyperparamcandidate, "batch_size");
        Map<String, Object> batchSizeRange = (Map<String, Object>) ASTConfLangCompilationUnitHandler.getValueByKey(searchSpace, "batch_size");

        int lower = (int) batchSizeRange.get("lower");
        int upper = (int) batchSizeRange.get("upper");

        assertTrue(lower <= batchSize);
        assertTrue(batchSize <= upper);
    }

    @Test
    public void testGetRandomOptimizer() {
        Map<String, Object> optimizerMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(newHyperparamcandidate, "optimizer");

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

}