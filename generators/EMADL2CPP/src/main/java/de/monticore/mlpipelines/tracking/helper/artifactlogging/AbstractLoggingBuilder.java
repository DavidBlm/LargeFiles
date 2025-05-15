package de.monticore.mlpipelines.tracking.helper.artifactlogging;

import de.monticore.mlpipelines.tracking.tracker.MultiBackendTracker;

public abstract class AbstractLoggingBuilder<T extends AbstractLoggingBuilder<T>> {

    protected MultiBackendTracker tracker;
    protected String artifactPath = "";

    public abstract void log();
    public abstract void logPersistent();

    protected AbstractLoggingBuilder(MultiBackendTracker tracker) {
        this.tracker = tracker;
    }

    public T setPath(String artifactPath) {
        this.artifactPath = artifactPath;
        return (T) this;
    }

    protected void clearAttributes() {
        this.artifactPath = "";
    }
}
