package de.monticore.lang.monticar.emadl.modularity;

import de.monticore.lang.monticar.emadl.AbstractSymtabTest;
import de.monticore.lang.monticar.emadl.generator.EMADLGeneratorCli;
import de.se_rwth.commons.logging.Log;
import org.junit.Before;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.fail;

public abstract class ModularTestSetup  extends AbstractSymtabTest {

    @Before
    public void setUp() {
        // ensure an empty log
        Log.initWARN();
        Log.getFindings().clear();
        Log.enableFailQuick(false);

    }

    public void removeMultipleTrainingHashes(String[] hashPaths)throws IOException{
        for (String path:hashPaths){
            removeDirectory(path);
        }
    }

    public void removeDirectory(String path) throws IOException{
        if (path.equals("")) {
            Log.info("Path empty","IO_INFO");
            return;
        }

        File files = new File(path);
        if (!files.exists()) {
            Log.info("Path does not exist","IO_INFO");
            return;
        }

        for (File file: files.listFiles()){
            if (file.isDirectory()) removeDirectory(file.getPath());

            boolean success = file.delete();
            if (!success) throw new IOException("File could not be deleted:" + file.toString());
        }
    }

    public void removeCNNFilesFromPreviousRuns(String[] hashPaths){
        try {
            removeMultipleTrainingHashes(hashPaths);
            removeDirectory("target/hashes");
            removeDirectory("model");
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void runGenerator(String[] args, String[] hashPaths, int expectedFindings, boolean exceptionAllowed){
        removeCNNFilesFromPreviousRuns(hashPaths);
        try {
            EMADLGeneratorCli.main(args);
            checkFindingsCount(expectedFindings);

            Log.getFindings().stream().forEach(finding -> {
                Log.info("FINDING: " + finding.toString(),"FINDINGS_LOG");
            });

        }catch (Exception e) {
            Log.info(e.toString(),"MODULAR_TEST_EXCEPTION");
            StackTraceElement[] stackTraceElements = e.getStackTrace();
            StringBuilder trace = new StringBuilder("Stack trace: \n");
            for (StackTraceElement element:stackTraceElements){
                trace.append(element.toString()).append("\n");
            }
            Log.error("MODULAR_TEST_EXCEPTION: " + trace.toString());

            if (!exceptionAllowed) {
                fail();
                throw new RuntimeException();
            }
        }
    }

}
