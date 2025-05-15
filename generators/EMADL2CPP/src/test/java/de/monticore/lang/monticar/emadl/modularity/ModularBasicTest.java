package de.monticore.lang.monticar.emadl.modularity;

import de.se_rwth.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

public class ModularBasicTest extends ModularTestSetup {
    private final String[] hashPaths = {"target/hashes"};
    @Before
    public void setUp() {
        Log.getFindings().clear();
        Log.enableFailQuick(false);
    }
    @Test
    public void testEmtpyNetwork() throws IOException {
        String[] args = {"-m", "src/test/resources/models/ModularCNN/emptyNetwork", "-r", "calculator.Connector", "-o", "target", "-b", "GLUON", "-c", "y", "-f", "n"};
        runGenerator(args, hashPaths,6,true);
    }

    @Test
    public void testModularNetworkSimpleMultiNet() throws IOException {
        String[] args = {"-m", "src/test/resources/models/ModularCNN/modularNetworkSimpleMultiNet", "-r", "calculator.Connector", "-o", "target", "-b", "GLUON", "-c", "y", "-f", "n"};
        runGenerator(args, hashPaths,7,false);
    }

    @Test
    public void testModularNetworkComplex() throws IOException {
        String[] args = {"-m", "src/test/resources/models/ModularCNN/modularNetworkComplex", "-r", "calculator.Connector", "-o", "target", "-b", "GLUON", "-c", "y", "-f", "n"};
        runGenerator(args, hashPaths,6,false);
    }

    @Test
    public void testModularNetworkSimple() throws IOException {
        String[] args = {"-m", "src/test/resources/models/ModularCNN/modularNetworkSimple", "-r", "calculator.Connector", "-o", "target", "-b", "GLUON", "-c", "y", "-f", "n"};
        runGenerator(args, hashPaths,6,false);
    }

    @Test
    public void testSingleNetwork() throws IOException {
        String[] args = {"-m", "src/test/resources/models/ModularCNN/singleNetwork", "-r", "calculator.Connector", "-o", "target", "-b", "GLUON", "-c", "y", "-f", "n"};
        runGenerator(args, hashPaths,6,false);
    }
}
