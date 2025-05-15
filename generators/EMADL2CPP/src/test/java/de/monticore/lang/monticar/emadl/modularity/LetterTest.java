package de.monticore.lang.monticar.emadl.modularity;

import de.se_rwth.commons.logging.Log;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;

public class LetterTest extends ModularTestSetup {

    String[] hashPaths = {"target/letterPredictor"};

    @Test
    public void testLetterPredictorModular() throws IOException {
        Log.getFindings().clear();
        removeDirectory("model");
        removeDirectory("target/letterpred");
        String[] args = {"-m", "src/test/resources/models/ModularCNN/letterPredictor", "-r", "letterpred.Connector", "-o", "target", "-b", "GLUON", "-c", "y",  "-f", "n"};
        runGenerator(args, hashPaths,1,false);
    }


    @Test
    public void testLetterPredictorSingle() throws IOException {
        Log.getFindings().clear();
        removeDirectory("model");
        removeDirectory("target/letterpred");
        String[] args = {"-m", "src/test/resources/models/ModularCNN/letterPredictorSingle", "-r", "letterpred.Connector", "-o", "target", "-b", "GLUON", "-c", "y", "-f", "n"};
        runGenerator(args, hashPaths,1,false);
    }


}
