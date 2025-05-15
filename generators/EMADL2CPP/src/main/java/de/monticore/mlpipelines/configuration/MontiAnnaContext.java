package de.monticore.mlpipelines.configuration;

import de.monticore.lang.monticar.emadl.generator.Backend;
import de.monticore.mlpipelines.tracking.TrackerFactory;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class MontiAnnaContext {

    private Path parentModelPath;

    private String rootModelName;

    private Path pipelineReferenceModelsPath = Paths.get("src/main/resources/pipelines/");

    private ExperimentConfiguration experimentConfiguration;

    private final Backend targetBackend = Backend.PYTORCH;
    private TrackerFactory trackerFactory;

    private static MontiAnnaContext montiAnnaConfiguration;
    private MontiAnnaContext() {
    }

    public static MontiAnnaContext getInstance() {
        if (montiAnnaConfiguration == null) {
            montiAnnaConfiguration = new MontiAnnaContext();
        }
        return montiAnnaConfiguration;
    }

    public Path getParentModelPath() {
        return parentModelPath;
    }

    public String getRootModelName() {
        return rootModelName;
    }

    public Backend getTargetBackend() {
        return targetBackend;
    }

    public ExperimentConfiguration getExperimentConfiguration() {
        return experimentConfiguration;
    }

    public void initContext(final Path parentModelPath,
            final String rootModelName,
            final ExperimentConfiguration experimentConfiguration,
            final TrackerFactory trackerFactory) {
        getInstance().parentModelPath = parentModelPath;
        getInstance().rootModelName = rootModelName;
        getInstance().experimentConfiguration = experimentConfiguration;
        getInstance().trackerFactory = trackerFactory;
    }

    public void setPipelineReferenceModelsPath(final Path pipelineReferenceModelsPath) {
        this.pipelineReferenceModelsPath = pipelineReferenceModelsPath;
    }

    public Path getPipelineReferenceModelsPath() {
        return pipelineReferenceModelsPath;
    }

    public TrackerFactory getTrackerFactory() {
        return trackerFactory;
    }
}
