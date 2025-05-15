package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class AdaNetAlgorithmTest extends TestCase {
    String modelPath = "src/test/resources/models/adanet/EfficientNet.emadl";

    @Test
    public void testConstructor() {
        AdaNetAlgorithm adaNet = new AdaNetAlgorithm();
        assertNotNull(adaNet);
    }

    @Test
    public void testExecuteThrowsExceptionWhenTrainPipelineNotSet() {
        ArchitectureSymbol arch = mock(ArchitectureSymbol.class);
        AdaNetAlgorithm adanet = new AdaNetAlgorithm();
        boolean exceptionThrown = false;
        try {
            adanet.execute(arch);
            fail("Expected exception");
        } catch (IllegalStateException e) {
            exceptionThrown = true;
            assertEquals("Train pipeline not set", e.getMessage());
        }

        assertTrue(exceptionThrown);
    }
}