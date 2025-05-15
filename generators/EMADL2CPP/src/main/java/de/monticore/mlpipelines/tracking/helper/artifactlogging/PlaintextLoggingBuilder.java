package de.monticore.mlpipelines.tracking.helper.artifactlogging;

import de.monticore.mlpipelines.tracking.tracker.MultiBackendTracker;
import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;

public class PlaintextLoggingBuilder extends SingleArtifactLoggingBuilder<PlaintextLoggingBuilder> {

    private String content;

    protected PlaintextLoggingBuilder(MultiBackendTracker tracker, String content) {
        super(tracker);
        this.content = content;
    }

    @Override
    public void log() {
        if(fileName == null) {
            Log.error("No file name for plaintext artifact has been set prior to calling log()");
            return;
        }

        try {
            Path tempDir = Files.createTempDirectory("emadl2cpp");
            File tempFile = tempDir.resolve(fileName).toFile();

            FileWriter writer = new FileWriter(tempFile);
            writer.write(content);
            writer.close();

            tracker.logArtifact(tempFile, artifactPath);

            FileUtils.deleteDirectory(tempDir.toFile());
        } catch (IOException e) {
            Log.error("Logging of plaintext artifact " + fileName + " failed: Could not create or delete temporary file");
        }
        clearAttributes();
    }

    @Override
    public void logPersistent() {
        if(fileName == null) {
            Log.error("No file name for plaintext artifact has been set prior to calling addPersistent()");
            return;
        }

        try {
            Path tempDir = Files.createTempDirectory("emadl2cpp");
            File tempFile = tempDir.resolve(fileName).toFile();

            FileWriter writer = new FileWriter(tempFile);
            writer.write(content);
            writer.close();

            tracker.addPersistentArtifact(tempFile, artifactPath);

            FileUtils.deleteDirectory(tempDir.toFile());
        } catch (IOException e) {
            Log.error("Logging of plaintext artifact " + fileName + " failed: Could not create or delete temporary file");
        }
        clearAttributes();
    }

    @Override
    protected void clearAttributes() {
        super.clearAttributes();
        this.content = null;
    }
}
