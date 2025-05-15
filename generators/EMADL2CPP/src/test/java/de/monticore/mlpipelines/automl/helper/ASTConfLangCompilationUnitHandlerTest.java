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
public class ASTConfLangCompilationUnitHandlerTest extends TestCase {

    ASTConfLangCompilationUnit compilationUnit;

    @Before
    public void setup() throws IOException {
        Path modelPath = Paths.get("src/test/resources/models/automl");
        String model = "Network.conf";

        ConfLangParser parser = new ConfLangParser();
        Path path = Paths.get(modelPath.toString(), model);
        compilationUnit = parser.parse(path.toString()).get();
    }

    @Test
    public void testGetNumEpochValue() {
        int numEpoch = (int) ASTConfLangCompilationUnitHandler.getValueByKey(this.compilationUnit, "num_epoch");
        assertEquals(numEpoch, 8);
    }

    @Test
    public void testGetBatchSizeValue() {
        int batchSize = (int) ASTConfLangCompilationUnitHandler.getValueByKey(this.compilationUnit, "batch_size");
        assertEquals(batchSize, 10);
    }

    @Test
    public void testGetNormalizeValue() {
        boolean normalize = (boolean) ASTConfLangCompilationUnitHandler.getValueByKey(this.compilationUnit, "normalize");
        assertFalse(normalize);
    }

    @Test
    public void testGetContextValue() {
        String context = (String) ASTConfLangCompilationUnitHandler.getValueByKey(this.compilationUnit, "context");
        assertEquals(context, "cpu");
    }

    @Test
    public void testGetNestedOptimizerValue() {
        Map<String, Object> configMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(this.compilationUnit, "optimizer");
        Map<String, Object> nestedMap = (Map<String, Object>) configMap.get("nestedMap");

        assertEquals(configMap.get("optimizer"), "adam");
        assertEquals(nestedMap.get("learning_rate"), 0.001);
        assertEquals(nestedMap.get("learning_rate_decay"), 0.8);
        assertEquals(nestedMap.get("step_size"), 1000);
        assertEquals(nestedMap.get("weight_decay"), 0.00);
    }

    @Test
    public void testSetValueForKey() {
        ASTConfLangCompilationUnit updatedCompilationUnit = ASTConfLangCompilationUnitHandler.setValueForKey(this.compilationUnit, "num_epoch", 50);
        int updatedNumEpoch = (int) ASTConfLangCompilationUnitHandler.getValueByKey(updatedCompilationUnit, "num_epoch");

        assertEquals(updatedNumEpoch, 50);
    }

    @Test
    public void testSetBoolValueForKey() {
        ASTConfLangCompilationUnit updatedCompilationUnit = ASTConfLangCompilationUnitHandler.setValueForKey(this.compilationUnit, "normalize", true);
        boolean updatedNormalize = (boolean) ASTConfLangCompilationUnitHandler.getValueByKey(updatedCompilationUnit, "normalize");

        assertTrue(updatedNormalize);
    }

    @Test
    public void testSetNestedValueForKeys() {
        ASTConfLangCompilationUnit updatedCompilationUnit = ASTConfLangCompilationUnitHandler.setNestedValueForKeys(this.compilationUnit, "optimizer", "learning_rate", 0.005);
        Map<String, Object> updatedConfigMap = ASTConfLangCompilationUnitHandler.getValuesFromNestedConfiguration(updatedCompilationUnit, "optimizer");
        Map<String, Object> updatedNestedMap = (Map<String, Object>) updatedConfigMap.get("nestedMap");
        double updatedLearningRate = (double) updatedNestedMap.get("learning_rate");

        assertEquals(updatedLearningRate, 0.005);
    }
}
