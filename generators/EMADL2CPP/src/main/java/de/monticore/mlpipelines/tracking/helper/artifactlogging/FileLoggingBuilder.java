package de.monticore.mlpipelines.tracking.helper.artifactlogging;

import de.monticore.mlpipelines.tracking.tracker.MultiBackendTracker;
import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.apache.commons.io.FileUtils;

public class FileLoggingBuilder extends SingleArtifactLoggingBuilder<FileLoggingBuilder> {

    private File artifact;

    protected FileLoggingBuilder(MultiBackendTracker tracker, File artifact) {
        super(tracker);
        this.artifact = artifact;
    }

    @Override
    public void log() {
        if(!artifact.exists()) {
            Log.error("Logging of artifact " + artifact.getAbsolutePath() + " failed: File does not exist.");
            return;
        }

        if(fileName != null) {
            try {
                Path tempDir = Files.createTempDirectory("emadl2cpp");
                File tempFile = tempDir.resolve(fileName).toFile();

                FileUtils.copyFile(artifact, tempFile);
                tracker.logArtifact(tempFile, artifactPath);

                FileUtils.deleteDirectory(tempDir.toFile());
            } catch (IOException e) {
                Log.error("Logging of artifact " + artifact.getAbsolutePath() + " failed: Could not create or delete temporary file.");
            }
        } else {
            tracker.logArtifact(artifact, artifactPath);
        }
        clearAttributes();
    }

    @Override
    public void logPersistent() {
        if(!artifact.exists()) {
            Log.error("Logging of artifact " + artifact.getAbsolutePath() + " failed: File does not exist.");
            return;
        }

        if(fileName != null) {
            try {
                Path tempDir = Files.createTempDirectory("emadl2cpp");
                File tempFile = tempDir.resolve(fileName).toFile();

                FileUtils.copyFile(artifact, tempFile);
                tracker.addPersistentArtifact(tempFile, artifactPath);

                FileUtils.deleteDirectory(tempDir.toFile());
            } catch (IOException e) {
                Log.error("Logging of artifact " + artifact.getAbsolutePath() + " failed: Could not create or delete temporary file.");
            }
        } else {
            tracker.addPersistentArtifact(artifact, artifactPath);
        }
        clearAttributes();
    }

    @Override
    protected void clearAttributes() {
        super.clearAttributes();
        this.artifact = null;
    }

}
