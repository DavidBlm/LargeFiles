/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl.integration;

import de.monticore.lang.monticar.emadl.AbstractSymtabTest;
import de.monticore.lang.monticar.emadl.generator.EMADLGeneratorCli;
import de.se_rwth.commons.logging.Log;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

public class IntegrationGluonTest extends IntegrationTest {

    public IntegrationGluonTest() {
        super("GLUON", "39253EC049D4A4E5FA0536AD34874B9D#1DBAEE1B1BD83FB7CB5F70AE91B29638#C4C23549E737A759721D6694C75D9771#5AF0CE68E408E8C1F000E49D72AC214A");
    }

    @Before
    public void setUp() {
        Log.initWARN();
        Log.getFindings().clear();
        Log.enableFailQuick(false);
    }

    @Test
    public void testModelWithoutCNN() {
        Log.getFindings().clear();


        String[] args = {"-m", "src/test/resources/models/", "-r", "Add", "-b", "GLUON"};
        EMADLGeneratorCli.main(args);

        AbstractSymtabTest.checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code/no_backend"),
                Arrays.asList(
                        "add.cpp",
                        "add.h",
                        "CMakeLists.txt",
                        "HelperA.h"
                )
        );
        assertTrue(Log.getFindings().isEmpty());
    }

    @Ignore
    @Test
    public void testMultipleStreams() {
        Log.getFindings().clear();

        deleteHashFile();

        String[] args = {"-m", "src/test/resources/models/", "-r", "MultipleStreams", "-b", "GLUON"};
        EMADLGeneratorCli.main(args);

        checkFindingsCount(1);
    }

    @Ignore
    @Test
    public void testRNNencdec() {
        Log.getFindings().clear();

        deleteHashFile();

        String[] args = {"-m", "src/test/resources/models", "-r", "rnnencdec.Main", "-b", "GLUON"};
        EMADLGeneratorCli.main(args);

        checkFindingsCount();
    }

    @Ignore
    @Test
    public void testRNNsearch() {
        Log.getFindings().clear();

        deleteHashFile();

        String[] args = {"-m", "src/test/resources/models", "-r", "rnnsearch.Main", "-b", "GLUON"};
        EMADLGeneratorCli.main(args);

        checkFindingsCount();
    }

    @Ignore
    @Test
    public void testShowAttendTell() {
        Log.getFindings().clear();

        deleteHashFile();

        String[] args = {"-m", "src/test/resources/models", "-r", "showAttendTell.Main", "-b", "GLUON"};
        EMADLGeneratorCli.main(args);

        checkFindingsCount(1);
    }

    @Test
    @Ignore // TODO fix
    public void testEpisodicMemorySimple() {
        Log.getFindings().clear();

        deleteHashFile();

        String[] args = {"-m", "src/test/resources/models", "-r", "episodicMemorySimple.Network", "-b", "GLUON", "-f", "y"};
        EMADLGeneratorCli.main(args);
    }

    @Test
    @Ignore // TODO fix
    public void testGluonPreprocessingWithSupervised() {
        Log.getFindings().clear();
        deleteHashFile();
        String[] args = {"-m", "src/test/resources/models/", "-r", "PreprocessingNetwork", "-b", "GLUON"};
        EMADLGeneratorCli.main(args);
        checkFindingsCount(1);
    }

    @Test
    @Ignore // TODO fix
    public void testGluonPreprocessingWithGAN() {
        Log.getFindings().clear();
        deleteHashFile();
        String[] args = {"-m", "src/test/resources/models/ganModel", "-r", "defaultGANPreprocessing.GeneratorWithPreprocessing", "-b", "GLUON"};
        EMADLGeneratorCli.main(args);

        checkFindingsCount(1);
    }

    @Test
    public void testMNISTCalculatorWithCustomLayer() {
        Log.getFindings().clear();

        deleteHashFile();

        String[] args = {"-m", "src/test/resources/models/customMNISTCalculator", "-r", "cNNCalculator.Connector", "-b", "GLUON", "-cfp", "src/test/resources/custom_files"};
        EMADLGeneratorCli.main(args);

        checkFindingsCount(6);
    }

    private void deleteHashFile() {
        try {
            Files.delete(Paths.get("./target/generated-sources-emadl/hashes/hashes.json"));
        }
        catch (NoSuchFileException e) {

        }
        catch(Exception e) {
            fail("Could not delete hash file");
        }
    }
}