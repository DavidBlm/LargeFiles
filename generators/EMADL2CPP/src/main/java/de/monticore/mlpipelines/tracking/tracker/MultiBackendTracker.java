package de.monticore.mlpipelines.tracking.tracker;

import de.monticore.mlpipelines.tracking.TrackingBackend;
import de.monticore.mlpipelines.tracking.helper.PersistentMetadataStore;
import de.monticore.mlpipelines.tracking.helper.SimpleRegexMatcher;
import de.monticore.mlpipelines.tracking.helper.artifactlogging.ArtifactHandler;
import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A {@link RunTracker} that forwards all calls to multiple other {@link RunTracker}s.
 */
public class MultiBackendTracker implements RunTracker {

    private final List<RunTracker> trackers;
    private final List<TrackingBackend> trackingBackends;
    private final ArtifactHandler artifactHandler = new ArtifactHandler(this);
    private final PersistentMetadataStore persistentData = new PersistentMetadataStore();


    // Tags set in the tracking configuration file. Must be logged every run and therefore be re-added after clearPersistentData() is called.
    private Map<String, String> manualTags;
    private SimpleRegexMatcher parameterBlacklist;

    public MultiBackendTracker(List<RunTracker> trackers,
            List<TrackingBackend> trackingBackends,
            Map<String, String> manualTags,
            SimpleRegexMatcher parameterBlacklist) {
        this.trackers = trackers;
        this.trackingBackends = trackingBackends;
        this.manualTags = manualTags;
        this.parameterBlacklist = parameterBlacklist;

        persistentData.addTags(manualTags);
    }

    @Override
    public void startNewRun() {
        for(RunTracker tracker : trackers) {
            tracker.startNewRun();
        }

        // Log persistent data
        for(Entry<String, String> entry : persistentData.getParams().entrySet()) {
            logParam(entry.getKey(), entry.getValue());
        }
        for(Entry<String, String> entry : persistentData.getTags().entrySet()) {
            logTag(entry.getKey(), entry.getValue());
        }
        for(Entry<File, String> entry : persistentData.getArtifacts().entrySet()) {
            logArtifact(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void endRun() {
        for(RunTracker tracker : trackers) {
            tracker.endRun();
        }
    }

    @Override
    public void close() {
        for(RunTracker tracker : trackers) {
            tracker.cleanUpRepository();
            tracker.close();
        }
        persistentData.close();
    }

    // ================== Parameter logging ==================
    @Override
    public void logParam(String key, String value) {
        if(parameterBlacklist.matchesAny(key)) {
            return;
        }
        for(RunTracker tracker : trackers) {
            tracker.logParam(key, value);
        }
    }

    public void logParams(Map<String, String> params) {
        for(Entry<String, String> entry : params.entrySet()) {
            logParam(entry.getKey(), entry.getValue());
        }
    }

    public void addPersistentParam(String key, String value) {
        if(parameterBlacklist.matchesAny(key)) {
            return;
        }
        persistentData.addParam(key, value);
    }

    public void addPersistentParams(Map<String, String> params) {
        persistentData.addParams(params.entrySet().stream().filter(e -> !parameterBlacklist.matchesAny(e.getKey()))
                .collect(HashMap::new, (m, v) -> m.put(v.getKey(), v.getValue()), Map::putAll));
    }

    public void removePersistentParam(String key) {
        persistentData.removeParam(key);
    }

    // ================== Metric logging ==================

    @Override
    public void logMetric(String key, double value, long timestamp, int step) {
        for(RunTracker tracker : trackers) {
            tracker.logMetric(key, value, timestamp, step);
        }
    }

    public void logMetric(String key, double value) {
        logMetric(key, value, System.currentTimeMillis(), 0);
    }

    public void logMetric(String key, double value, long timestamp) {
        logMetric(key, value, timestamp, 0);
    }

    public void logMetric(String key, double value, int step) {
        logMetric(key, value, System.currentTimeMillis(), step);
    }


    // ================== Tag logging ==================
    @Override
    public void logTag(String key, String value) {
        for(RunTracker tracker : trackers) {
            tracker.logTag(key, value);
        }
    }

    public void logTags(Map<String, String> tags) {
        for(Entry<String, String> entry : tags.entrySet()) {
            logTag(entry.getKey(), entry.getValue());
        }
    }

    public void addPersistentTag(String key, String value) {
        persistentData.addTag(key, value);
    }

    public void addPersistentTags(Map<String, String> tagsToAdd) {
        persistentData.addTags(tagsToAdd);
    }

    public void removePersistentTag(String key) {
        persistentData.removeTag(key);
    }


    // ================== Artifact logging ==================
    @Override
    public void logArtifact(File file, String artifactPath) {
        for(RunTracker tracker : trackers) {
            tracker.logArtifact(file, artifactPath);
        }
    }

    public void addPersistentArtifact(File file, String artifactPath) {
        persistentData.addArtifact(file, artifactPath);
    }

    public ArtifactHandler getArtifactHandler() {
        return artifactHandler;
    }

    // ======================================================

    public void clearPersistentData() {
        persistentData.clear();

        // Manual tags must not be cleared
        addPersistentTags(manualTags);
    }

    public void cleanUpRepository() {
        for(RunTracker tracker : trackers) {
            tracker.cleanUpRepository();
        }
    }

    @Override
    public Map<String, String> getPythonParams() {
        Map<String, String> params = new HashMap<>();
        for(RunTracker tracker : trackers) {
            for(Entry<String, String> entry : tracker.getPythonParams().entrySet()) {
                if(params.containsKey(entry.getKey())){
                    Log.error("Duplicate key in python params: " + entry.getKey());
                    continue;
                }
                params.put(entry.getKey(), entry.getValue());
            }
        }
        if(!trackingBackends.isEmpty()) {
            params.put("tracking_backends", trackingBackends.stream().map(TrackingBackend::toString).reduce((a, b) -> a + "," + b).orElse(""));
        }
        return params;
    }

}
