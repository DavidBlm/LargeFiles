package de.monticore.mlpipelines.python;

import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;
import de.monticore.mlpipelines.configuration.ExperimentConfiguration;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;
import de.monticore.mlpipelines.pipelines.PythonPipeline;
import de.monticore.mlpipelines.tracking.TrackerFactory;
import org.junit.Assert;
import org.junit.Test;
class PythonPipelineTest {

    @Test
    void createSchemaApiPathFromLearningMethod() {
        final ExperimentConfiguration experimentConfiguration = new ExperimentConfiguration(
                "src/test/resources/experiment/configuration", "", "", "");
        MontiAnnaContext.getInstance().initContext(null, null, experimentConfiguration, new TrackerFactory(null));
        final PythonPipeline pythonPipeline = new PythonPipeline(LearningMethod.SUPERVISED);
        final String schemaApiPathFromLearningMethod = pythonPipeline.createSchemaApiPathFromLearningMethod()
                .toString();
        final String expectedPath = "src/test/resources/experiment/configuration/Supervised_Schema_API";
        Assert.assertEquals(expectedPath, schemaApiPathFromLearningMethod);
    }

    //TODO implement me
    @Test
    void generateTrainingConfiguration() {
    }

    //TODO implement me
    @Test
    void generatePipelineExecutionScript() {
    }

    //TODO implement me
    @Test
    void execute() {
    }
}