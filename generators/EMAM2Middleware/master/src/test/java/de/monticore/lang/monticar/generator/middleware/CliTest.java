/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware;

import de.monticore.lang.embeddedmontiarc.LogConfig;
import de.monticore.lang.monticar.generator.middleware.cli.DistributedTargetGeneratorCli;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.*;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


public class CliTest {
    private static final String VALID_MODELS_DIR_OPTION = "src/test/resources/";
    private static final String VALID_ROOT_MODEL_OPTION = "tests.a.addComp";
    private static final List<String> VALID_GENERATOR_CPP_OPTION = Arrays.asList("cpp");
    private static final List<String> VALID_GENERATOR_ALL_OPTION = Arrays.asList("cpp", "roscpp", "odv");

    private static final String INVALID_MODELS_DIR_OPTION = "src/invalid/resources/";
    private static final String INVALID_ROOT_MODEL_OPTION = "invalid.invalid.AddComp";
    private static final List<String> INVALID_GENERATOR_OPTION = Arrays.asList("invalid");
    private static final List<String> INVALID_GENERATOR_EMPTY_OPTION = new ArrayList<>();
    public static final String RESNET_MODELNAME = "tests.emadlTests.resNet34";

    private String buildParameterJson(String modelsDir, String rootModel, Collection<String> generators, String outputDir) {
        return buildParameterJson(modelsDir, rootModel, generators, outputDir, null, false);
    }

    private String buildParameterJson(String modelsDir, String rootModel, Collection<String> generators, String outputDir, String emadlBackend, boolean writeTagFile) {
        String result = "{";
        result += "'modelsDir': '" + modelsDir + "', ";
        result += "'rootModel': '" + rootModel + "', ";
        result += "'generators': [" + generators.stream().map(g -> "'" + g + "'").collect(Collectors.joining(", ")) + "], ";
        if (emadlBackend != null) {
            result += "'emadlBackend': '" + emadlBackend + "', ";
        }
        result += "'writeTagFile':'" + writeTagFile + "',";
        result += "'outputDir': '" + outputDir + "'";
        result += "}";
        return result;
    }

    @BeforeClass
    public static void initLog() {
        LogConfig.init();
        LogConfig.enableFailQuick(false);
    }

    @Before
    public void clearFindings() {
        LogConfig.getFindings().clear();
    }

    @AfterClass
    public static void resetLog() {
        LogConfig.getFindings().clear();
        LogConfig.enableFailQuick(true);
    }


    @Test
    public void testParserError() {
        String[] args = {
                "invalidParsingArg"};

        DistributedTargetGeneratorCli.main(args);
        assertTrue(LogConfig.getErrorCount() == 1);
        assertTrue(LogConfig.getFindings().stream().map(Finding::getMsg).anyMatch(msg -> msg.contains("0x49E6A")));
    }

    @Test
    public void testNoGenerators() {
        String json = buildParameterJson(
                VALID_MODELS_DIR_OPTION,
                VALID_ROOT_MODEL_OPTION,
                INVALID_GENERATOR_EMPTY_OPTION,
                "target/cliTest/NoGenerators/");

        DistributedTargetGeneratorCli.main(new String[]{"-r", json});
        assertTrue(LogConfig.getErrorCount() == 1);
        assertTrue(LogConfig.getFindings().stream().map(Finding::getMsg).anyMatch(msg -> msg.contains("0x6178E")));
    }

    @Test
    public void testInvalidGenerator() {
        String json = buildParameterJson(
                VALID_MODELS_DIR_OPTION,
                VALID_ROOT_MODEL_OPTION,
                INVALID_GENERATOR_OPTION,
                "target/cliTest/InvalidGenerators/");

        DistributedTargetGeneratorCli.main(new String[]{"-r", json});
        assertTrue(LogConfig.getErrorCount() == 1);
        assertTrue(logContains("0xE28B6"));
    }

    @Test
    public void testOneGenerator() {
        String targetDir = "target/cliTest/OneGenerator/";
        String json = buildParameterJson(
                VALID_MODELS_DIR_OPTION,
                VALID_ROOT_MODEL_OPTION,
                VALID_GENERATOR_CPP_OPTION,
                targetDir);

        DistributedTargetGeneratorCli.main(new String[]{"-r", json});
        String[] positiveFileNames = {
                "CMakeLists.txt",
                "tests_a_addComp/cpp/tests_a_addComp.h",
                "tests_a_addComp/cpp/CMakeLists.txt",
                "tests_a_addComp/coordinator/CMakeLists.txt",
                "tests_a_addComp/coordinator/Coordinator_tests_a_addComp.cpp"
        };

        for (String positiveFileName : positiveFileNames) {
            assertTrue(Files.exists(Paths.get(targetDir + "src/" + positiveFileName)));
        }
    }

    @Test
    public void testSingleEMADLGenerator() {
        String targetDir = "target/cliTest/SingleEmadlTest/";
        String json = buildParameterJson(
                VALID_MODELS_DIR_OPTION,
                RESNET_MODELNAME,
                Arrays.asList("emadlcpp"),
                targetDir,
                "MXNET",
                false);

        DistributedTargetGeneratorCli.main(new String[]{"-r", json});

        String[] positiveFileNames = getEMADLGeneratedFilesList(false);

        for (String positiveFileName : positiveFileNames) {
            assertTrue(Files.exists(Paths.get(targetDir + "src/" + positiveFileName)));
        }
    }

    @Test
    public void testEMADLAndRosGenerator(){
        String targetDir = "target/cliTest/EmadlRosTest/";
        String json = buildParameterJson(
                VALID_MODELS_DIR_OPTION,
                RESNET_MODELNAME,
                Arrays.asList("emadlcpp","roscpp"),
                targetDir,
                "MXNET",
                false);

        DistributedTargetGeneratorCli.main(new String[]{"-r", json});
        String[] positiveFileNames = getEMADLGeneratedFilesList(true);

        for (String positiveFileName : positiveFileNames) {
            assertTrue(Files.exists(Paths.get(targetDir + "src/" + positiveFileName)));
        }
    }

    @Test
    public void testEMADLConfigFile(){
        String targetDir = "target/cliTest/emadlConfigFile/";

        DistributedTargetGeneratorCli.main(new String[]{"src/test/resources/config/emadl.json"});
        String[] positiveFileNames = getEMADLGeneratedFilesList(true);

        for (String positiveFileName : positiveFileNames) {
            assertTrue(Files.exists(Paths.get(targetDir + "src/" + positiveFileName)));
        }
    }

    @Test
    public void testInvalidConfigFile(){
        DistributedTargetGeneratorCli.main(new String[]{"src/test/resources/config/invalid.json"});
        assertTrue(Log.getErrorCount() > 0);
    }

    private String[] getEMADLGeneratedFilesList(boolean generateRosFiles) {
        String modelName = RESNET_MODELNAME.replace('.', '_');
        String[] generatedFiles = {
                modelName + "/CMakeLists.txt",

                modelName + "/cpp/CNNBufferFile.h",
                modelName + "/cpp/CNNCreator_" + modelName + ".py",
                modelName + "/cpp/CNNPredictor_" + modelName + ".h",
                modelName + "/cpp/CNNTrainer_" + modelName + ".py",
                modelName + "/cpp/CNNTranslator.h",
                modelName + "/cpp/HelperA.h",
                modelName + "/cpp/CMakeLists.txt",
                modelName + "/cpp/" + modelName + ".h",

                modelName + "/coordinator/CMakeLists.txt",
                modelName + "/coordinator/Coordinator_" + modelName + ".cpp",
                modelName + "/coordinator/IAdapter_" + modelName + ".h"
        };
        if (generateRosFiles) {
            String[] generatedRosFiles = {
                    modelName + "/roscpp/CMakeLists.txt",
                    modelName + "/roscpp/RosAdapter_" + modelName + ".h"
            };
            generatedFiles = ArrayUtils.addAll(generatedFiles, generatedRosFiles);
        }
        return generatedFiles;
    }

    @Test
    public void testValidConfigFile(){
        String[] args = {"src/test/resources/config/valid.json"};
        DistributedTargetGeneratorCli.main(args);

        String[] positiveFileNames = {
                "CMakeLists.txt",
                "tests_a_addComp/cpp/tests_a_addComp.h",
                "tests_a_addComp/cpp/CMakeLists.txt",
                "tests_a_addComp/coordinator/CMakeLists.txt",
                "tests_a_addComp/coordinator/Coordinator_tests_a_addComp.cpp",
                "tests_a_addComp/roscpp/RosAdapter_tests_a_addComp.h",
                "tests_a_addComp/roscpp/CMakeLists.txt",
        };

        for (String positiveFileName : positiveFileNames) {
            assertTrue(Files.exists(Paths.get("target/cliTest/validConfigFile/src/" + positiveFileName)));
        }
    }

    @Test
    public void testAllGenerators(){
        String targetDir = "target/cliTest/AllGenerators/";
        String json = buildParameterJson(
                VALID_MODELS_DIR_OPTION,
                VALID_ROOT_MODEL_OPTION,
                VALID_GENERATOR_ALL_OPTION,
                targetDir);

        DistributedTargetGeneratorCli.main(new String[]{"-r", json});

        String[] positiveFileNames = {
                "CMakeLists.txt",
                "tests_a_addComp/cpp/tests_a_addComp.h",
                "tests_a_addComp/cpp/CMakeLists.txt",
                "tests_a_addComp/coordinator/CMakeLists.txt",
                "tests_a_addComp/coordinator/Coordinator_tests_a_addComp.cpp",
                "tests_a_addComp/roscpp/RosAdapter_tests_a_addComp.h",
                "tests_a_addComp/roscpp/CMakeLists.txt",
        };

        for (String positiveFileName : positiveFileNames) {
            assertTrue(Files.exists(Paths.get(targetDir + "src/" + positiveFileName)));
        }
    }

    @Test
    public void testInvalidRootModelError(){
        String targetDir = "target/cliTest/InvalidRootModel/";
        String json = buildParameterJson(
                VALID_MODELS_DIR_OPTION,
                INVALID_ROOT_MODEL_OPTION,
                VALID_GENERATOR_CPP_OPTION,
                targetDir);

        DistributedTargetGeneratorCli.main(new String[]{"-r", json});

        assertTrue(LogConfig.getErrorCount() == 1);
        assertTrue(logContains("0x5FFAE"));
    }

    @Test
    public void testInvalidModelsDirError(){
        String targetDir="target/cliTest/InvalidModelsDir/";
        String json = buildParameterJson(
                INVALID_MODELS_DIR_OPTION,
                VALID_ROOT_MODEL_OPTION,
                VALID_GENERATOR_CPP_OPTION,
                targetDir);

        DistributedTargetGeneratorCli.main(new String[]{"-r", json});
        assertTrue(LogConfig.getErrorCount() == 1);
        assertTrue(logContains("0x6444B"));
    }

    @Test
    public void testWriteTagFile() {
        String targetDir="target/cliTest/TagFile/";
        String json = buildParameterJson(
                VALID_MODELS_DIR_OPTION,
                VALID_ROOT_MODEL_OPTION,
                VALID_GENERATOR_ALL_OPTION,
                targetDir,
                null,
                true);

        DistributedTargetGeneratorCli.main(new String[]{"-r", json});

        assertTrue(Files.exists(Paths.get(targetDir, "emam","RosConnections.tag")));
    }

    @Test
    public void testWriteTagFileDisabled() {
        String targetDir="target/cliTest/TagFileDisabled/";
        String json = buildParameterJson(
                VALID_MODELS_DIR_OPTION,
                VALID_ROOT_MODEL_OPTION,
                VALID_GENERATOR_ALL_OPTION,
                targetDir,
                null,
                false);

        DistributedTargetGeneratorCli.main(new String[]{"-r", json});

        assertFalse(Files.exists(Paths.get(targetDir, "emam","RosConnections.tag")));
    }

    private boolean logContains(String errorCode) {
        return LogConfig.getFindings().stream().map(Finding::getMsg).anyMatch(msg -> msg.contains(errorCode));
    }

    @Test
    public void testRclcppGenerator(){
        String targetDir = "target/cliTest/AllGenerators/";
        String json = buildParameterJson(
                VALID_MODELS_DIR_OPTION,
                VALID_ROOT_MODEL_OPTION,
                Arrays.asList("cpp","rclcpp"),
                targetDir);

        DistributedTargetGeneratorCli.main(new String[]{"-r", json});

        String[] positiveFileNames = {
                "CMakeLists.txt",
                "tests_a_addComp/cpp/tests_a_addComp.h",
                "tests_a_addComp/cpp/CMakeLists.txt",
                "tests_a_addComp/coordinator/CMakeLists.txt",
                "tests_a_addComp/coordinator/Coordinator_tests_a_addComp.cpp",
                "tests_a_addComp/rclcpp/RosAdapter_tests_a_addComp.h",
                "tests_a_addComp/rclcpp/CMakeLists.txt",
        };

        for (String positiveFileName : positiveFileNames) {
            assertTrue(Files.exists(Paths.get(targetDir + "src/" + positiveFileName)));
        }
    }

    @Test
    public void testRos2cppGenerator(){
        String targetDir = "target/cliTest/AllGenerators/";
        String json = buildParameterJson(
                VALID_MODELS_DIR_OPTION,
                VALID_ROOT_MODEL_OPTION,
                Arrays.asList("cpp","ros2cpp"),
                targetDir);

        DistributedTargetGeneratorCli.main(new String[]{"-r", json});

        String[] positiveFileNames = {
                "CMakeLists.txt",
                "tests_a_addComp/cpp/tests_a_addComp.h",
                "tests_a_addComp/cpp/CMakeLists.txt",
                "tests_a_addComp/coordinator/CMakeLists.txt",
                "tests_a_addComp/coordinator/Coordinator_tests_a_addComp.cpp",
                "tests_a_addComp/rclcpp/RosAdapter_tests_a_addComp.h",
                "tests_a_addComp/rclcpp/CMakeLists.txt",
        };

        for (String positiveFileName : positiveFileNames) {
            assertTrue(Files.exists(Paths.get(targetDir + "src/" + positiveFileName)));
        }
    }

}
