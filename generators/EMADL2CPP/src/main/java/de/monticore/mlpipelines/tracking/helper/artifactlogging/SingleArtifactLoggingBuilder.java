package de.monticore.mlpipelines.tracking.helper.artifactlogging;

import de.monticore.mlpipelines.tracking.tracker.MultiBackendTracker;

public abstract class SingleArtifactLoggingBuilder<T extends SingleArtifactLoggingBuilder<T>> extends AbstractLoggingBuilder<T> {

    protected String fileName;

    protected SingleArtifactLoggingBuilder(MultiBackendTracker tracker) {
        super(tracker);
    }

    public T setFileName(String fileName) {
        this.fileName = fileName;
        return (T) this;
    }

    @Override
    protected void clearAttributes() {
        super.clearAttributes();
        this.fileName = null;
    }

}
