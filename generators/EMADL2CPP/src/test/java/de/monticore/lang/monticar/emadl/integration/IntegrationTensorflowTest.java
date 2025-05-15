/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl.integration;

import de.monticore.lang.monticar.emadl.AbstractSymtabTest;
import de.monticore.lang.monticar.emadl.generator.EMADLGeneratorCli;
import de.se_rwth.commons.logging.Log;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

@Ignore
public class IntegrationTensorflowTest extends IntegrationTest {

    private final Path multipleStreamsHashFile = Paths.get("./target/generated-sources-emadl/MultipleStreams.training_hash");

    public IntegrationTensorflowTest() {
        super("TENSORFLOW", "39253EC049D4A4E5FA0536AD34874B9D#1DBAEE1B1BD83FB7CB5F70AE91B29638#C4C23549E737A759721D6694C75D9771#5AF0CE68E408E8C1F000E49D72AC214A");
    }

    @Test
    public void testModelWithoutCNN() {
        Log.getFindings().clear();


        String[] args = {"-m", "src/test/resources/models/", "-r", "Add", "-b", "TENSORFLOW"};
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

    @Test
    public void testMultipleStreams() {
        Log.getFindings().clear();

        deleteHashFile();

        String[] args = {"-m", "src/test/resources/models/", "-r", "MultipleStreams", "-b", "TENSORFLOW"};
        EMADLGeneratorCli.main(args);

        checkFindingsCount();
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
