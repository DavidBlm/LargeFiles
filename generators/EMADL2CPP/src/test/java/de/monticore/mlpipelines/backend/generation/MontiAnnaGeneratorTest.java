package de.monticore.mlpipelines.backend.generation;

import de.monticore.mlpipelines.configuration.ExperimentConfiguration;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Arrays;

import static de.monticore.mlpipelines.util.TestUtil.*;


public class MontiAnnaGeneratorTest extends BackendTest {

    @Test
    public void testPyTorchBackendArtefactsGenerationWithEMADLGenerator() {
        final ExperimentConfiguration experimentConfiguration = new ExperimentConfiguration("./target/generated-sources-emadl");
        final MontiAnnaContext montiAnnaContext = initialiseContext(Paths.get("src/test/resources/models/"), "mnist.mnistClassifier", experimentConfiguration);
        new MontiAnnaGenerator(montiAnnaContext).generateTargetBackendArtefacts();
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