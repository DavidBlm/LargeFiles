package de.monticore.lang.monticar.emadl.modularity;

import de.se_rwth.commons.logging.Log;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

public class ModularSentimentTest extends ModularTestSetup{
    String[] hashPaths = {"target/singleNetwork","target/emptyNetwork","target/modularNetworkComplex",
            "target/modularNetworkSimple","target/modularNetworkSimpleMultiNet","target/modularSentiment", "target/calculator", "target/sentimentanalyzer"};


    @Ignore
    @Test
    public void testModularSentiment() throws IOException {
        Log.getFindings().clear();
        removeDirectory("target/sentimentanalyzer");
        String[] args = {"-m", "src/test/resources/models/ModularCNN/modularSentiment", "-r", "sentimentanalyzer.Connector", "-o", "target", "-b", "GLUON", "-c", "y", "-f", "n"};
        runGenerator(args, hashPaths,2,false);
    }

    @Ignore
    @Test
    public void testSingleSentiment() throws IOException {
        Log.getFindings().clear();
        removeDirectory("target/sentimentanalyzer");
        String[] args = {"-m", "src/test/resources/models/ModularCNN/singleSentiment", "-r", "sentimentanalyzer.Connector", "-o", "target", "-b", "GLUON", "-c", "y", "-f", "n"};
        runGenerator(args, hashPaths,2,false);
    }


}
