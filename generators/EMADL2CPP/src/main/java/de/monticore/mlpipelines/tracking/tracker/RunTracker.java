package de.monticore.mlpipelines.tracking.tracker;

import java.io.File;
import java.util.Map;

public interface RunTracker {

    void close();
    void startNewRun();
    void endRun();
    void logParam(String key, String value);
    void logMetric(String key, double value, long timestamp, int step);
    void logArtifact(File file, String artifactPath);
    void logTag(String key, String value);

    /**
     * Executes the cleanup procedure of the tracker. This procedure is implemented by the tracker and may be enabled/disabled or customized in
     * the tracking configuration file. The cleanup is executed automatically upon calling the {@link #close()} method but may be invoked manually
     * at any time.
     */
    void cleanUpRepository();

    /**
     * Computes a map containing all parameters and their values that should be used to call the python script.
     * Each tracker can add its own parameters to the map.
     * @return A map containing all parameters and their values that should be used to call the python script.
     */
    Map<String, String> getPythonParams();
}
