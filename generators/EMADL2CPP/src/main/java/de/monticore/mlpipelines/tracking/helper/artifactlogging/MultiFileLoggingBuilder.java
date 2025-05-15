package de.monticore.mlpipelines.tracking.helper.artifactlogging;

import de.monticore.mlpipelines.tracking.tracker.MultiBackendTracker;
import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.apache.commons.io.FileUtils;

public class MultiFileLoggingBuilder extends AbstractLoggingBuilder<MultiFileLoggingBuilder> {

    private List<File> artifacts;
    private String newExtension;


    protected MultiFileLoggingBuilder(MultiBackendTracker tracker, List<File> artifacts) {
        super(tracker);
        this.artifacts = artifacts;
    }

    /**
     * Adds an extension to the end of the file name of the artifact.
     * @param newExtension New extension including the dot, e.g. ".txt"
     */
    public MultiFileLoggingBuilder addExtension(String newExtension) {
        this.newExtension = newExtension;
        return this;
    }

    @Override
    public void log() {
        for(File artifact : artifacts) {
            if(newExtension != null) {
                try {
                    Path tempDir = Files.createTempDirectory("emadl2cpp");
                    File tempFile = tempDir.resolve(artifact.getName() + newExtension).toFile();

                    FileUtils.copyFile(artifact, tempFile);
                    tracker.logArtifact(tempFile, artifactPath);

                    FileUtils.deleteDirectory(tempDir.toFile());
                } catch (IOException e) {
                    Log.error("Logging of artifact " + artifact.getAbsolutePath() + " failed: Could not create or delete temporary file.");
                }
            } else {
                tracker.logArtifact(artifact, artifactPath);
            }
        }
        clearAttributes();
    }

    @Override
    public void logPersistent() {
        for(File artifact : artifacts) {
            if(newExtension != null) {
                try {
                    Path tempDir = Files.createTempDirectory("emadl2cpp");
                    File tempFile = tempDir.resolve(artifact.getName() + newExtension).toFile();

                    FileUtils.copyFile(artifact, tempFile);
                    tracker.addPersistentArtifact(tempFile, artifactPath);

                    FileUtils.deleteDirectory(tempDir.toFile());
                } catch (IOException e) {
                    Log.error("Logging of artifact " + artifact.getAbsolutePath() + " failed: Could not create or delete temporary file.");
                }
            } else {
                tracker.addPersistentArtifact(artifact, artifactPath);
            }
        }
        clearAttributes();
    }

    @Override
    protected void clearAttributes() {
        super.clearAttributes();
        this.artifacts = null;
        this.newExtension = null;
    }
}
