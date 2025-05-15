/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl;

import de.monticore.lang.monticar.emadl.generator.EMADLGeneratorCli;
import de.se_rwth.commons.logging.Log;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Paths;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class CustomLayerTest extends AbstractSymtabTest {

    @Before
    public void setUp() {
        Log.initWARN();
        Log.getFindings().clear();
        Log.enableFailQuick(false);
    }

    @Test(expected = NoSuchElementException.class)
    public void NegativeCustomLayerTest (){
        String[] args = {"-m", "src/test/resources/models/customMNISTCalculator", "-r", "cNNCalculator.Connector", "-b", "GLUON", "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
        assertThrows(NoSuchElementException.class, () -> EMADLGeneratorCli.main(args));

    }

    @Ignore
    @Test(expected = NoSuchElementException.class)
    public void NegativeCustomLayerPyTorchTest (){
        String[] args = {"-m", "src/test/resources/models/customMNISTCalculator", "-r", "cNNCalculator.Connector", "-b", "PYTORCH", "-f", "n", "-c", "n"};
        assertThrows(NoSuchElementException.class, () -> EMADLGeneratorCli.main(args));
    }
    @Test
    public void CustomLayerTestPyTorch (){
        String[] args = {"-m", "src/test/resources/models/mnist", "-r", "LeNetNetwork", "-b", "PYTORCH",  "-f", "n", "-c", "n", "-cfp", "src/test/resources/custom_files"};
        EMADLGeneratorCli.main(args);
        assertTrue(Log.getFindings().isEmpty());

        checkFilesAreEqual(
                Paths.get("./target/generated-sources-emadl"),
                Paths.get("./src/test/resources/target_code/pytorch/customlayer"),
                Arrays.asList(
                        "CNNNet_customNetworkMnist.py"
                ));
    }
}