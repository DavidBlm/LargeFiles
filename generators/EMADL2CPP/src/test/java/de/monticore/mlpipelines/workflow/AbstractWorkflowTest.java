package de.monticore.mlpipelines.workflow;

import de.monticore.mlpipelines.backend.generation.MontiAnnaGenerator;
import de.monticore.mlpipelines.configuration.ExperimentConfiguration;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Arrays;

import static de.monticore.mlpipelines.util.TestUtil.*;

class AbstractWorkflowTest {

    @Test
    void generateBackendArtefactsIntoExperiment() {
        final MontiAnnaContext montiAnnaContext = initialiseContext(Paths.get("src/test/resources/models/"),
                "mnist.mnistClassifier", new ExperimentConfiguration("./target/generated-sources-emadl"));
        final DummyWorkflow dummyWorkflow = new DummyWorkflow();
        dummyWorkflow.setMontiAnnaGenerator(new MontiAnnaGenerator(montiAnnaContext));
        dummyWorkflow.generateBackendArtefactsIntoExperiment();
        checkFindingsCount();

        checkFilesAreEqual(
                Paths.get(montiAnnaContext.getExperimentConfiguration().getGenerationTargetPath()),
                Paths.get("./src/test/resources/target_code/pytorch/mnist"),
                Arrays.asList(
                        "mnist_mnistClassifier.cpp",
                        "mnist_mnistClassifier.h",
                        "CNNNet_mnist_mnistClassifier_net.py",
                        "mnist_mnistClassifier_net.h",
                        "CNNTranslator.h",
                        "mnist_mnistClassifier_calculateClass.h",
                        "execute_mnist_mnistClassifier_net",
                        "CNNPredictor_mnist_mnistClassifier_net.h"));
    }
}