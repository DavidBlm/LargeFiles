/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl.integration;

import de.monticore.lang.monticar.emadl.AbstractSymtabTest;
import de.monticore.lang.monticar.emadl.generator.EMADLGeneratorCli;
import de.se_rwth.commons.logging.Log;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Arrays;

import static junit.framework.TestCase.assertTrue;

@Ignore
public class IntegrationCaffe2Test extends IntegrationTest {
    public IntegrationCaffe2Test() {
        super("CAFFE2", "39253EC049D4A4E5FA0536AD34874B9D#1DBAEE1B1BD83FB7CB5F70AE91B29638#13D139510DC5681639AA91D7250288D3#1A42D4842D0664937A9F6B727BD60CEF");
    }

    @Test
    public void testModelWithoutCNN() {
        Log.getFindings().clear();


        String[] args = {"-m", "src/test/resources/models/", "-r", "Add", "-b", "CAFFE2"};
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
}
