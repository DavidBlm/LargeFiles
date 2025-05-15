package de.monticore.mlpipelines.tracking.helper;

import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.io.FileUtils;

public class PersistentMetadataStore {

    private final Map<String, String> parameters = new HashMap<>();
    private final Map<String, String> tags = new HashMap<>();

    // Maps absolute path on localmachine to relative path on the tracking server
    private final Map<String, String> artifacts = new HashMap<>();

    private Path tempDir;

    public PersistentMetadataStore() {
        try {
            tempDir = Files.createTempDirectory("emadl2cpp-persistent-logging");
        } catch (IOException e) {
            Log.error("Failed to create temporary directory for persistent logging data", e);
        }
    }

    public void addParam(String key, String value) {
        parameters.put(key, value);
    }

    public void addParams(Map<String, String> params) {
        parameters.putAll(params);
    }

    public void removeParam(String key) {
        parameters.remove(key);
    }

    public void addTag(String key, String value) {
        tags.put(key, value);
    }

    public void addTags(Map<String, String> tagsToAdd) {
        tags.putAll(tagsToAdd);
    }

    public void removeTag(String key) {
        tags.remove(key);
    }

    public void addArtifact(File file, String path) {
        try {
            File tempFile = tempDir.resolve(path).resolve(file.getName()).toFile();
            FileUtils.copyFile(file, tempFile);
            artifacts.put(tempFile.getAbsolutePath(), path);
        } catch (IOException e) {
            Log.error("Logging of artifact " + file.getAbsolutePath() + " failed: Could not create temporary file");
        }
    }

    public void removeArtifact(File file) {
        artifacts.remove(file.getAbsolutePath());
        // Deleting quietly is okay, because the temporary directory of the OS will be cleared on reboot anyway
        FileUtils.deleteQuietly(file);
    }

    /**
     * Clears all data to prepare for the next set of runs
     */
    public void clear() {
        parameters.clear();
        tags.clear();
        artifacts.clear();
        try {
            FileUtils.cleanDirectory(tempDir.toFile());
        } catch (IOException e) {
            Log.error("Failed to clear temporary directory for persistent logging data", e);
        }
    }

    /**
     * Clears all data and then deletes the temporary directory.
     * After calling this method, the object should not be used anymore.
     */
    public void close() {
        clear();
        // Deleting quietly is okay, because the temporary directory of the OS will be cleared on reboot anyway
        FileUtils.deleteQuietly(tempDir.toFile());
    }

    public Map<String, String> getParams() {
        return parameters;
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public Map<File, String> getArtifacts() {
        Map<File, String> res = new HashMap<>();
        for (Map.Entry<String, String> entry : artifacts.entrySet()) {
            res.put(new File(entry.getKey()), entry.getValue());
        }
        return res;
    }

}
