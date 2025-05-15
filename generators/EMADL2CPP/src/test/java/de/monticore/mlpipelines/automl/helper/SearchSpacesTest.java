package de.monticore.mlpipelines.automl.helper;

import conflang._ast.ASTConfLangCompilationUnit;
import conflang._parser.ConfLangParser;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@RunWith(MockitoJUnitRunner.class)
public class SearchSpacesTest extends TestCase {

    ASTConfLangCompilationUnit compilationUnit;

    @Before
    public void setup() throws IOException {
        Path modelPath = Paths.get("src/test/resources/models/automl/searchspaces");
        String model = "Network.conf";

        ConfLangParser parser = new ConfLangParser();
        Path path = Paths.get(modelPath.toString(), model);
        compilationUnit = parser.parse(path.toString()).get();
    }

    @Test
    public void testSet() {
        ASTConfLangCompilationUnitHandler.setValueForKey(compilationUnit, "num_epoch", 10);
    }

    @Test
    public void testGetNumEpochRange() {
        Map<String, Object> numEpochRange = (Map<String, Object>) ASTConfLangCompilationUnitHandler.getValueByKey(this.compilationUnit, "num_epoch");
        assertEquals(numEpochRange.get("lower"), 8);
        assertEquals(numEpochRange.get("step_size"), 1);
        assertEquals(numEpochRange.get("upper"), 100);
    }

    @Test
    public void testGetBatchSizeRange() {
        Map<String, Object> batchSizeRange = (Map<String, Object>) ASTConfLangCompilationUnitHandler.getValueByKey(this.compilationUnit, "batch_size");
        assertEquals(batchSizeRange.get("lower"), 10);
        assertEquals(batchSizeRange.get("upper"), 128);
    }

    @Test
    public void testGetNestedOptimizerValue() {
        Map<String, Object> configMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(this.compilationUnit, "optimizer");
        Map<String, Object> nestedMap = (Map<String, Object>) configMap.get("nestedMap");

        Map<String, Object> learningRateMap = (Map<String, Object>) nestedMap.get("learning_rate");
        Map<String, Object> stepSizeMap = (Map<String, Object>) nestedMap.get("step_size");
        Map<String, Object> weightDecayMap = (Map<String, Object>) nestedMap.get("weight_decay");


        assertEquals(configMap.get("optimizer"), "adam");

        assertEquals(learningRateMap.get("lower"), 0.001);
        assertEquals(learningRateMap.get("step_size"), 0.0001);
        assertEquals(learningRateMap.get("upper"), 0.01);

        assertEquals(stepSizeMap.get("lower"), 100);
        assertEquals(stepSizeMap.get("step_size"), 10);
        assertEquals(stepSizeMap.get("upper"), 1000);

        assertEquals(weightDecayMap.get("lower"), 0.0);
        assertEquals(weightDecayMap.get("step_size"), 0.01);
        assertEquals(weightDecayMap.get("upper"), 0.9);
    }

    @Test
    public void testgetAllKeys() {
        Map<String, Boolean> keysMap = ASTConfLangCompilationUnitHandler.getAllKeys(compilationUnit);

        assertFalse(keysMap.get("num_epoch"));
        assertFalse(keysMap.get("batch_size"));
        assertTrue(keysMap.get("optimizer"));
    }

    @Test
    public void testSetNumEpochValue() {
        ASTConfLangCompilationUnit newCompilationUnit = compilationUnit.deepClone();
        ASTConfLangCompilationUnit updatedCompilationUnit = ASTConfLangCompilationUnitHandler.setValueForKey(newCompilationUnit, "num_epoch", 20);

        int updatedNumEpoch = (int) ASTConfLangCompilationUnitHandler.getValueByKey(updatedCompilationUnit, "num_epoch");
        Object numEpochRange = ASTConfLangCompilationUnitHandler.getValueByKey(compilationUnit, "num_epoch");

        assertEquals(updatedNumEpoch, 20);
        assertFalse(numEpochRange.equals(20));
    }

    @Test
    public void testSetNestedValueForKeys() {
        ASTConfLangCompilationUnit newCompilationUnit = compilationUnit.deepClone();
        ASTConfLangCompilationUnit updatedCompilationUnit = ASTConfLangCompilationUnitHandler.setNestedValueForKeys(newCompilationUnit, "optimizer", "learning_rate", 0.005);
        Map<String, Object> updatedConfigMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(updatedCompilationUnit, "optimizer");
        Map<String, Object> updatedNestedMap = (Map<String, Object>) updatedConfigMap.get("nestedMap");
        double updatedLearningRate = (double) updatedNestedMap.get("learning_rate");

        assertEquals(updatedLearningRate, 0.005);
    }
}
