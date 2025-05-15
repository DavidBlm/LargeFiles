package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.mlpipelines.pipelines.Pipeline;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;


@RunWith(MockitoJUnitRunner.class)
public class EfficientNetTest extends TestCase {
    @Test
    public void testConstructor() {
        EfficientNet efficientNet = createEfficientNet();
        assertNotNull(efficientNet);
    }

    private EfficientNet createEfficientNet() {
        Pipeline pipeline = mock(Pipeline.class);
//        EfficientNetConfig config = createEfficientNetConfig();
        EfficientNet efficientNet = new EfficientNet();
        efficientNet.setTrainPipeline(pipeline);
        efficientNet.setTrainConfiguration(null);
        return efficientNet;
    }
}