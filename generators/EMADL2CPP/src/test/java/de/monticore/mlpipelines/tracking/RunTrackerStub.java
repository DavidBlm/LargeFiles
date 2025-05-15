package de.monticore.mlpipelines.tracking;

import de.monticore.mlpipelines.tracking.tracker.RunTracker;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

class RunTrackerStub implements RunTracker {

    private final Map<String, String> tags = new HashMap<>();
    private final Map<String, String> params = new HashMap<>();
    private final Map<String, Double> metrics = new HashMap<>();
    private final Map<File, String> artifacts = new HashMap<>();

    @Override
    public void logParam(String key, String value) {
        params.put(key, value);
    }

    @Override
    public void logMetric(String key, double value, long timestamp, int step) {
        metrics.put(key, value);
    }

    @Override
    public void logArtifact(File file, String artifactPath) {
        artifacts.put(file, artifactPath);
    }

    @Override
    public void logTag(String key, String value) {
        tags.put(key, value);
    }

    @Override
    public void close() {}

    @Override
    public void startNewRun() {}

    @Override
    public void endRun() {}

    @Override
    public void cleanUpRepository() {}

    @Override
    public Map<String, String> getPythonParams() {
        return new HashMap<>();
    }

    public Map<String, String> getTags() {
        return tags;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public Map<String, Double> getMetrics() {
        return metrics;
    }

    public Map<File, String> getArtifacts() {
        return artifacts;
    }
}
