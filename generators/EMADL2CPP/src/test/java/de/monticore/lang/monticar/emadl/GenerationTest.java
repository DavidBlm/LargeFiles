/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl;

import de.monticore.lang.monticar.emadl.generator.Backend;
import de.monticore.lang.monticar.emadl.generator.EMADLGenerator;
import de.monticore.lang.monticar.emadl.generator.EMADLGeneratorCli;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;
import freemarker.template.TemplateException;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

public class GenerationTest extends AbstractSymtabTest {

    @Before
    public void setUp() {
        Log.initWARN();
        Log.getFindings().clear();
        Log.enableFailQuick(false);
    }

    @Test
    public void testCifar10Generation() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = { "-m", "src/test/resources/models/", "-r", "cifar10.Cifar10Classifier", "-b", "MXNET", "-f",
                "n", "-c", "n" };
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().isEmpty());

        checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code"),
                Arrays.asList(
                        "cifar10_cifar10Classifier.cpp",
                        "cifar10_cifar10Classifier.h",
                        "CNNCreator_cifar10_cifar10Classifier_net.py",
                        "CNNModelLoader.h",
                        "CNNPredictor_cifar10_cifar10Classifier_net.h",
                        "cifar10_cifar10Classifier_net.h",
                        "CNNTranslator.h",
                        "cifar10_cifar10Classifier_calculateClass.h",
                        "CNNTrainer_cifar10_cifar10Classifier_net.py"));
    }

    @Test
    public void testSimulatorGeneration() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "simulator.MainController", "-b", "MXNET", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        checkFindingsCount();
    }

    @Test
    public void testAddGeneration() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "Add", "-b", "MXNET", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        checkFindingsCount();
    }

    @Test
    public void testAlexnetGeneration() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "Alexnet", "-b", "MXNET", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        checkFindingsCount();
    }

    @Test
    public void testResNeXtGeneration() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "ResNeXt50", "-b", "MXNET", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        checkFindingsCount();
    }

    @Test
    public void testThreeInputGeneration() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "ThreeInputCNN_M14", "-b", "MXNET", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        checkFindingsCount(1L);
    }

    @Test
    public void testMultipleOutputsGeneration() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "MultipleOutputs", "-b", "MXNET", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        checkFindingsCount(1L);
    }

    @Test
    public void testVGGGeneration() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "VGG16", "-b", "MXNET", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        checkFindingsCount();
    }

    @Test
    public void testEpisodicMemorySimpleGeneration() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models", "-r", "episodicMemorySimple.Network", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
    }

    @Test
    public void testMultipleInstances() throws IOException, TemplateException {
        try {
            Log.getFindings().clear();
            String[] args = {"-m", "src/test/resources/models/", "-r", "InstanceTest.MainB", "-b", "MXNET", "-f", "n", "-c", "n"};
            EMADLGeneratorCli.main(args);
            checkFindingsCount();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMnistClassifierForCaffe2() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "mnist.MnistClassifier", "-b", "CAFFE2", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        checkFindingsCount();

        checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code"),
                Arrays.asList(
                        "mnist_mnistClassifier.cpp",
                        "mnist_mnistClassifier.h",
                        "CNNCreator_mnist_mnistClassifier_net.py",
                        "CNNPredictor_mnist_mnistClassifier_net.h",
                        "mnist_mnistClassifier_net.h",
                        "CNNTranslator.h",
                        "mnist_mnistClassifier_calculateClass.h",
                        "CNNTrainer_mnist_mnistClassifier_net.py"));
    }

    @Ignore
    @Test
    public void testMnistClassifierForTensorflow() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "mnist.MnistClassifier", "-b", "TENSORFLOW", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        checkFindingsCount();

        checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code/tensorflow"),
                Arrays.asList(
                        "mnist_mnistClassifier.cpp",
                        "mnist_mnistClassifier.h",
                        "CNNCreator_mnist_mnistClassifier_net.py",
                        "CNNPredictor_mnist_mnistClassifier_net.h",
                        "CNNDataLoader_mnist_mnistClassifier_net.py",
                        "mnist_mnistClassifier_net.h",
                        "HelperA.h",
                        "CNNTranslator.h",
                        "mnist_mnistClassifier_calculateClass.h",
                        "CNNTrainer_mnist_mnistClassifier_net.py"));
    }

    @Ignore
    @Test
    public void testMnistClassifierForGluon() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "mnist.MnistClassifier", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        checkFindingsCount();
        checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code/gluon"),
                Arrays.asList(
                        "CNNModelLoader.h",
                        "CNNNet_mnist_mnistClassifier_net.py",
                        "mnist_mnistClassifier.cpp",
                        "mnist_mnistClassifier.h",
                        "CNNCreator_mnist_mnistClassifier_net.py",
                        "CNNPredictor_mnist_mnistClassifier_net.h",
                        "CNNDataLoader_mnist_mnistClassifier_net.py",
                        "CNNSupervisedTrainer_mnist_mnistClassifier_net.py",
                        "HelperA.h",
                        "CNNTranslator.h",
                        "mnist_mnistClassifier_calculateClass.h",
                        "CNNTrainer_mnist_mnistClassifier_net.py",
                        "mnist_mnistClassifier_net.h"));
    }

    @Test
    public void testInvariantForGluon() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "Invariant", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        checkFindingsCount();
    }

    @Test
    public void testGluonReinforcementModelGymEnvironment() {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/reinforcementModel", "-r", "cartpole.Master", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().stream().filter(Finding::isError).collect(Collectors.toList()).isEmpty());
        checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code/gluon/reinforcementModel/cartpole"),
                Arrays.asList(
                        "cartpole_master.cpp",
                        "cartpole_master.h",
                        "cartpole_master_dqn.h",
                        "cartpole_master_policy.h",
                        //"CMakeLists.txt",
                        "CNNModelLoader.h",
                        "CNNCreator_cartpole_master_dqn.py",
                        "CNNNet_cartpole_master_dqn.py",
                        "CNNPredictor_cartpole_master_dqn.h",
                        "CNNTrainer_cartpole_master_dqn.py",
                        "CNNTranslator.h",
                        "HelperA.h",
                        "start_training.sh",
                        "reinforcement_learning/__init__.py",
                        "reinforcement_learning/strategy.py",
                        "reinforcement_learning/agent.py",
                        "reinforcement_learning/environment.py",
                        "reinforcement_learning/replay_memory.py",
                        "reinforcement_learning/util.py",
                        "reinforcement_learning/cnnarch_logger.py"
                )
        );
    }

    @Test
    public void testHashFunction() {
        EMADLGenerator tester = new EMADLGenerator(Backend.MXNET,"./target");

        try {
            tester.getEmadlFileHandler().getChecksumForFile("invalid Path!");
            fail("Hash method should throw IOException on invalid path");
        } catch (IOException e) {
        }
    }

    @Test
    public void gluonDdpgTest() {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/reinforcementModel", "-r", "mountaincar.Master", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);

        assertEquals(0, Log.getFindings().stream().filter(Finding::isError).count());
        checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code/gluon/reinforcementModel/mountaincar"),
                Arrays.asList(
                        "mountaincar_master.cpp",
                        "mountaincar_master.h",
                        "mountaincar_master_actor.h",
                        //"CMakeLists.txt",
                        "CNNModelLoader.h",
                        "CNNCreator_mountaincar_master_actor.py",
                        "CNNNet_mountaincar_master_actor.py",
                        "CNNPredictor_mountaincar_master_actor.h",
                        "CNNTrainer_mountaincar_master_actor.py",
                        "CNNTranslator.h",
                        "HelperA.h",
                        "start_training.sh",
                        "reinforcement_learning/__init__.py",
                        "reinforcement_learning/CNNCreator_mountaincar_agent_mountaincarCritic.py",
                        "reinforcement_learning/CNNNet_mountaincar_agent_mountaincarCritic.py",
                        "reinforcement_learning/strategy.py",
                        "reinforcement_learning/agent.py",
                        "reinforcement_learning/environment.py",
                        "reinforcement_learning/replay_memory.py",
                        "reinforcement_learning/util.py",
                        "reinforcement_learning/cnnarch_logger.py"
                )
        );
    }

    @Test
    public void testGluonDefaultGANGeneration() {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/ganModel", "-r", "defaultGAN.DefaultGANConnector", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().stream().filter(Finding::isError).collect(Collectors.toList()).isEmpty());
        checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code/gluon/ganModel/defaultGAN"),
                Arrays.asList(
                        "gan/CNNCreator_defaultGAN_defaultGANDiscriminator.py",
                        "gan/CNNNet_defaultGAN_defaultGANDiscriminator.py",
                        "CNNCreator_defaultGAN_defaultGANConnector_predictor.py",
                        "CNNGanTrainer_defaultGAN_defaultGANConnector_predictor.py",
                        "CNNNet_defaultGAN_defaultGANConnector_predictor.py",
                        "CNNPredictor_defaultGAN_defaultGANConnector_predictor.h",
                        "CNNTrainer_defaultGAN_defaultGANConnector_predictor.py",
                        "defaultGAN_defaultGANConnector.cpp",
                        "defaultGAN_defaultGANConnector.h",
                        "defaultGAN_defaultGANConnector_predictor.h"
                )
        );
    }

    @Test
    public void testGluonInfoGANGeneration() {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/ganModel", "-r", "infoGAN.InfoGANConnector", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().stream().filter(Finding::isError).collect(Collectors.toList()).isEmpty());
        checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code/gluon/ganModel/infoGAN"),
                Arrays.asList(
                        "gan/CNNCreator_infoGAN_infoGANDiscriminator.py",
                        "gan/CNNNet_infoGAN_infoGANDiscriminator.py",
                        "gan/CNNCreator_infoGAN_infoGANQNetwork.py",
                        "gan/CNNNet_infoGAN_infoGANQNetwork.py",
                        "CNNCreator_infoGAN_infoGANConnector_predictor.py",
                        "CNNDataLoader_infoGAN_infoGANConnector_predictor.py",
                        "CNNGanTrainer_infoGAN_infoGANConnector_predictor.py",
                        "CNNNet_infoGAN_infoGANConnector_predictor.py",
                        "CNNPredictor_infoGAN_infoGANConnector_predictor.h",
                        "CNNTrainer_infoGAN_infoGANConnector_predictor.py",
                        "infoGAN_infoGANConnector.cpp",
                        "infoGAN_infoGANConnector.h",
                        "infoGAN_infoGANConnector_predictor.h"
                )
        );
    }

    @Test
    public void testGluonCVAEGeneration() {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/vaes", "-r", "cvae.Connector", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().stream().filter(Finding::isError).collect(Collectors.toList()).isEmpty());
        checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code/gluon/vaes/cvae"),
                Arrays.asList(
                        "CNNAutoencoderTrainer_cvae_encoder.py",
                        "CNNCreator_cvae_encoder.py",
                        "CNNNet_cvae_encoder.py",
                        "CNNCreator_cvae_connector_decoder.py",
                        "CNNDataLoader_cvae_connector_decoder.py",
                        "CNNNet_cvae_connector_decoder.py",
                        "CNNPredictor_cvae_connector_decoder.h",
                        "CNNTrainer_cvae_connector_decoder.py",
                        "cvae_connector.cpp",
                        "cvae_connector.h",
                        "cvae_connector_decoder.h"
                )
        );
    }

    @Test
    public void testGluonVQVAEGeneration() {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/vaes", "-r", "vqvae.Connector", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().stream().filter(Finding::isError).collect(Collectors.toList()).isEmpty());
        checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code/gluon/vaes/vqvae"),
                Arrays.asList(
                        "CNNAutoencoderTrainer_vqvae_encoder.py",
                        "CNNCreator_vqvae_encoder.py",
                        "CNNNet_vqvae_encoder.py",
                        "CNNCreator_vqvae_connector_decoder.py",
                        "CNNDataLoader_vqvae_connector_decoder.py",
                        "CNNNet_vqvae_connector_decoder.py",
                        "CNNPredictor_vqvae_connector_decoder.h",
                        "CNNTrainer_vqvae_connector_decoder.py",
                        "vqvae_connector.cpp",
                        "vqvae_connector.h",
                        "vqvae_connector_decoder.h"
                )
        );
    }

    @Test
    public void testGluonVAEGeneration() {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/vaes", "-r", "vae.Connector", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().stream().filter(Finding::isError).collect(Collectors.toList()).isEmpty());
        checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code/gluon/vaes/vae"),
                Arrays.asList(
                        "CNNAutoencoderTrainer_vae_encoder.py",
                        "CNNCreator_vae_encoder.py",
                        "CNNNet_vae_encoder.py",
                        "CNNCreator_vae_connector_decoder.py",
                        "CNNDataLoader_vae_connector_decoder.py",
                        "CNNNet_vae_connector_decoder.py",
                        "CNNPredictor_vae_connector_decoder.h",
                        "CNNTrainer_vae_connector_decoder.py",
                        "vae_connector.cpp",
                        "vae_connector.h",
                        "vae_connector_decoder.h"
                )
        );
    }

    @Test
    public void testGluonCoraDglGeneration() {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/gnns", "-r", "coraDgl.DGLNetwork", "-b", "GLUON", "-f", "n", "-c", "n", "-dgl", "y"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().stream().filter(Finding::isError).collect(Collectors.toList()).isEmpty());
        checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code/gluon/gnns/coraDgl"),
                Arrays.asList(
                        "CNNNet_coraDgl_dGLNetwork.py",
                        "CNNCreator_coraDgl_dGLNetwork.py",
                        "CNNDataLoader_coraDgl_dGLNetwork.py",
                        "CNNNet_coraDgl_dGLNetwork.py",
                        "CNNTrainer_coraDgl_dGLNetwork.py"
                )
        );
    }

    @Test
    public void testGluonPreprocessingWithSupervised() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "PreprocessingNetwork", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        Log.info(Log.getFindings().toString(), "testGluonPreprocessinWithSupervised");
        checkFindingsCount();
    }

    @Test
    public void testGluonPreprocessingWithGAN() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/ganModel", "-r", "defaultGANPreprocessing.GeneratorWithPreprocessing", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        Log.info(Log.getFindings().toString(), "testGluonPreprocessingWithGAN");
        checkFindingsCount();
    }

    @Test
    public void testAlexNetTagging() {
        Log.getFindings().clear();
        String[] args = { "-m", "src/test/resources/models/", "-r", "tagging.Alexnet", "-b", "MXNET", "-f", "n", "-c",
                "n" };
        EMADLGeneratorCli.main(args);
        checkFindingsCount(1L);
        assertEquals(Log.getFindings().get(0).toString(),
                "Tagging info for DataPath symbol was found, ignoring data_paths.txt: src/test/resources/models");
        assertEquals(0, Log.getErrorCount());
    }

    @Test
    public void testAlexNetTaggingForInstances() {
        Log.getFindings().clear();
        String[] args = { "-m", "src/test/resources/models/", "-r", "tagging.Parent", "-b", "MXNET", "-f", "n", "-c",
                "n" };
        EMADLGeneratorCli.main(args);
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidPathCoCos() {
        Log.getFindings().clear();
        String[] args = { "-m", "src/test/resources/models/", "-r", "tagging.AlexnetInvalid", "-b", "MXNET", "-f", "n",
                "-c", "n" };
        EMADLGeneratorCli.main(args);
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidPathCoCosInstances() {
        Log.getFindings().clear();
        String[] args = { "-m", "src/test/resources/models/", "-r", "tagging.ParentInvalidPath", "-b", "MXNET", "-f",
                "n", "-c", "n" };
        EMADLGeneratorCli.main(args);
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidTypeCocos() {

        Log.getFindings().clear();
        String[] args = { "-m", "src/test/resources/models/", "-r", "tagging.AlexnetInvalidType", "-b", "MXNET", "-f",
                "n", "-c", "n" };
        EMADLGeneratorCli.main(args);
    }

    @Test(expected = RuntimeException.class)
    public void testInvalidTypeCocosInstances() {

        Log.getFindings().clear();
        String[] args = { "-m", "src/test/resources/models/", "-r", "tagging.ParentInvalidType", "-b", "MXNET", "-f",
                "n", "-c", "n" };
        EMADLGeneratorCli.main(args);
    }

    @Test
    public void testMnistCalculatorWithCustomLayerForGluon() throws IOException, TemplateException {
        Log.getFindings().clear();
        //Test depends on the python version
        String[] args = {"-m", "src/test/resources/models/customMNISTCalculator", "-r", "cNNCalculator.Connector", "-b", "GLUON", "-cfp", "src/test/resources/custom_files", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        checkFindingsCount();
        checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code/gluon/customMNISTCalculator"),
                Arrays.asList(
                        "CNNNet_cNNCalculator_connector_predictor1.py",
                        "CNNCreator_cNNCalculator_connector_predictor1.py",
                        "CNNTrainer_cNNCalculator_connector_predictor1.py"));
    }

    @Test
    public void checkTrainedRlNetworkHasExactlyOneInputOutputDdpg() {
        String[] args = {"-m", "src/test/resources/models/architectureChecks", "-r", "ddpg.Master", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);

        /* Assert */
        List<Finding> errors = Log.getFindings().stream().filter(Finding::isError).collect(Collectors.toList());
        assertNotNull(errors);
        assertFalse(errors.isEmpty());

        Finding errorInputPorts = Finding.error("0xTA02C Component 'ddpg.agent.MountaincarActor' has the input ports " +
                "'state, state2', but is only allowed to have the ports 'state'.");
        Finding errorOutputPorts = Finding.error("0xTA03C Component 'ddpg.agent.MountaincarActor' has the output ports " +
                "'action, action2', but is only allowed to have the ports 'action'.");
        assertThat(errors, hasItem(errorInputPorts));
        assertThat(errors, hasItem(errorOutputPorts));
    }

    @Test
    public void checkTrainedRlNetworkHasExactlyOneInputOutputDqn() {
        String[] args = {"-m", "src/test/resources/models/architectureChecks", "-r", "dqn.Master", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);

        /* Assert */
        List<Finding> errors = Log.getFindings().stream().filter(Finding::isError).collect(Collectors.toList());
        assertNotNull(errors);
        assertFalse(errors.isEmpty());

        Finding errorInputPorts = Finding.error("0xTA02C Component 'dqn.agent.CartPoleDQN' has the input ports " +
                "'state, state2', but is only allowed to have the ports 'state'.");
        Finding errorOutputPorts = Finding.error("0xTA03C Component 'dqn.agent.CartPoleDQN' has the output ports " +
                "'qvalues, qvalues2', but is only allowed to have the ports 'qvalues'.");
        assertThat(errors, hasItem(errorInputPorts));
        assertThat(errors, hasItem(errorOutputPorts));
    }

    @Test
    public void checkRlTrainedArchitectureHasVectorOutput() {
        String[] args = {"-m", "src/test/resources/models/architectureChecks", "-r", "ddpg2.Master", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);

        /* Assert */
        List<Finding> errors = Log.getFindings().stream().filter(Finding::isError).collect(Collectors.toList());
        assertNotNull(errors);
        assertFalse(errors.isEmpty());

        Finding errorOutputPorts = Finding.error("0xTA06C Port 'action' of component 'ddpg2.agent.MountaincarActor' is " +
                "expected to be 1-dimensional, but it is 2-dimensional.");
        assertThat(errors, hasItem(errorOutputPorts));
    }

    @Test
    public void checkCriticNetworkHasExactlyOneDimensionalOutput() {
        String[] args = {"-m", "src/test/resources/models/architectureChecks", "-r", "ddpg3.Master", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);

        /* Assert */
        List<Finding> errors = Log.getFindings().stream().filter(Finding::isError).collect(
                Collectors.toList());
        assertNotNull(errors);
        assertFalse(errors.isEmpty());

        Finding errorOutputPorts = Finding.error("0xTA03C Component 'ddpg3.agent.MountaincarCritic' has the output " +
                "ports 'qvalues, qvalues2', but is only allowed to have the ports 'qvalues'.");
        Finding errorOutputDimension = Finding.error("0xTA06C Port 'qvalues' of component " +
                "'ddpg3.agent.MountaincarCritic' is expected to be 1-dimensional, but it is 2-dimensional.");
        assertThat(errors, hasItem(errorOutputPorts));
        assertThat(errors, hasItem(errorOutputDimension));
    }

    @Test
    public void checkCriticNetworkInputs() {
        String[] args = {"-m", "src/test/resources/models/architectureChecks", "-r", "ddpg4.Master", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);

        /* Assert */
        List<Finding> errors = Log.getFindings().stream().filter(Finding::isError).collect(
                Collectors.toList());
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        assertThat(errors, hasItems(
                Finding.error("0xTA02C Component 'ddpg4.agent.MountaincarCritic' has the input ports 'state, " +
                        "action, action2', but is only allowed to have the ports 'state, action'."),
                Finding.error("0xTA07C Port dimensions of components 'Actor' and 'Critic' do not match for " +
                        "connection 'actor.action -> critic.action'."),
                Finding.error("0xTA11C Port ranges of components 'Actor' and 'Critic' do not match for " +
                        "connection 'actor.action -> critic.action'."),
                Finding.error("0xTA08C Port types of components 'Actor' and 'Critic' do not match for " +
                        "connection 'actor.action -> critic.action'."))
        );
    }

    @Test
    public void checkActorCriticRequiresCriticNetwork() {
        String[] args = {"-m", "src/test/resources/models/architectureChecks", "-r", "ddpg5.Master", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);

        /* Assert */
        List<Finding> errors = Log.getFindings().stream().filter(Finding::isError).collect(
                Collectors.toList());
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        assertThat(errors, hasItem(
                Finding.error("0xTA01C No component with name 'critic' is available."))
        );
    }

    @Test
    public void checkGANGeneratorQNetworkDependency() {
        String[] args = {"-m", "src/test/resources/models/architectureChecks", "-r", "infoGAN.InfoGANConnector", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);

        /* Assert */
        List<Finding> errors = Log.getFindings().stream().filter(Finding::isError).collect(
                Collectors.toList());
        assertNotNull(errors);
        assertFalse(errors.isEmpty());
        assertThat(errors, hasItem(
                Finding.error("0xTA22C Incompatible array port connection 'qnetwork.c[1] -> generator.c[1]' " +
                        "between components 'infoGAN.InfoGANQNetwork' and 'infoGAN.InfoGANGenerator'. " +
                        "Component 'infoGAN.InfoGANQNetwork' has 1 output ports, whereas component " +
                        "'infoGAN.InfoGANGenerator' has 0 input ports."))
        );
    }

    @Test
    public void testCifar10GenerationPyTorch() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = { "-m", "src/test/resources/models/", "-r", "cifar10.Cifar10Classifier", "-b", "PYTORCH", "-f",
                "n", "-c", "n" };
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().isEmpty());

        checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code/pytorch/cifar"),
                Arrays.asList(
                        "cifar10_cifar10Classifier.cpp",
                        "cifar10_cifar10Classifier.h",
                        "CNNNet_cifar10_cifar10Classifier_net.py",
                        "CNNPredictor_cifar10_cifar10Classifier_net.h",
                        "cifar10_cifar10Classifier_net.h",
                        "CNNTranslator.h",
                        "cifar10_cifar10Classifier_calculateClass.h",
                        "execute_cifar10_cifar10Classifier_net"));
    }

    @Test
    public void testMnistClassifierForPyTorch() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "mnist.MnistClassifier", "-b", "PYTORCH", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        checkFindingsCount();

        checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
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

    @Test
    public void testLoadNetworkMnistForPyTorch() throws IOException, TemplateException {
        Log.getFindings().clear();
        String[] args = {"-m", "src/test/resources/models/", "-r", "LoadNetworkMnist", "-b", "PYTORCH", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        checkFindingsCount();

        checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code/pytorch/loadlayer"),
                Arrays.asList(
                        "loadNetworkMnist.h",
                        "CNNNet_loadNetworkMnist.py",
                        "CNNTranslator.h",
                        "execute_loadNetworkMnist",
                        "CNNPredictor_loadNetworkMnist.h"));
    }
}