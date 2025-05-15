package de.monticore.lang.monticar.emadl.modularity;

import de.se_rwth.commons.logging.Log;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

public class DecompTest extends ModularTestSetup {

    String[] hashPaths = {"target/modularNetworkSimplePretrained"};

    @Ignore
    @Test
    public void testDecomposedNetworkTraining() throws IOException {
        Log.getFindings().clear();
        removeDirectory("target/modularNetworkSimplePretrained");
        String[] args = {"-m", "src/test/resources/models/ModularCNN/modularNetworkSimplePretrained", "-r", "calculator.Connector", "-o", "target", "-b", "GLUON", "-c", "y", "-ad", "-dn", "Network,Net1,Net2"};
        runGenerator(args, hashPaths,14,false);
    }
}
