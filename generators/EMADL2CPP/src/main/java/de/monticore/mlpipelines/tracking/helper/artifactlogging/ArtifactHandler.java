package de.monticore.mlpipelines.tracking.helper.artifactlogging;

import de.monticore.mlpipelines.tracking.tracker.MultiBackendTracker;
import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ArtifactHandler {

    protected MultiBackendTracker tracker;

    public ArtifactHandler(MultiBackendTracker tracker) {
        this.tracker = tracker;
    }

    public FileLoggingBuilder setArtifact(File artifact) {
        if(artifact.isDirectory()) {
            Log.error("Logging of artifact failed: " + artifact.getAbsolutePath() + " is a directory.");
            return null;
        }
        return new FileLoggingBuilder(tracker, artifact);
    }

    public PlaintextLoggingBuilder setPlaintext(String content) {
        return new PlaintextLoggingBuilder(tracker, content);
    }

    public MultiFileLoggingBuilder setArtifacts(List<File> artifacts) {
        return new MultiFileLoggingBuilder(tracker, artifacts);
    }

    public MultiFileLoggingBuilder setArtifacts(File artifactsDir) {
        if(!artifactsDir.isDirectory()) {
            Log.error("Logging of artifacts failed: " + artifactsDir.getAbsolutePath() + " is not a directory.");
            return new MultiFileLoggingBuilder(tracker, Collections.emptyList());
        }

        if(!artifactsDir.exists()) {
            Log.error("Logging of artifacts failed: Directory " + artifactsDir.getAbsolutePath() + " does not exist.");
            return new MultiFileLoggingBuilder(tracker, Collections.emptyList());
        }

        return setArtifacts(listFilesInDirectory(artifactsDir));
    }

    private List<File> listFilesInDirectory(File directory) {
        List<File> res = new ArrayList<>();
        for(File file : Objects.requireNonNull(directory.listFiles())) {
            if(file.isFile()) {
                res.add(file);
            }
        }
        return res;
    }

}
