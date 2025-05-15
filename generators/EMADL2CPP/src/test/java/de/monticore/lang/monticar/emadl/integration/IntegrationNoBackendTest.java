/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl.integration;

import de.monticore.lang.monticar.emadl.AbstractSymtabTest;
import de.monticore.lang.monticar.emadl.generator.EMADLGeneratorCli;
import de.se_rwth.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;

public class IntegrationNoBackendTest extends AbstractSymtabTest {

    public IntegrationNoBackendTest() {

    }

    @Before
    public void setUp() {
        Log.initWARN();
        Log.getFindings().clear();
        Log.enableFailQuick(false);
    }

    @Test
    public void testErrorOut() {
        Log.getFindings().clear();
        Boolean threwException = false;
        String[] args = {"-m", "src/test/resources/models/", "-r", "MultipleStreams", "-b", "NONE"};

        try {
            EMADLGeneratorCli.main(args);
        }catch (Exception e){
            // We should have an exception.
            threwException = true;
        }
        assertTrue(threwException);
    }

    @Test
    public void testModelWithoutCNN() {
        Log.getFindings().clear();


        String[] args = {"-m", "src/test/resources/models/", "-r", "Add", "-b", "NONE"};
        EMADLGeneratorCli.main(args);

        checkFilesAreEqual(
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
}