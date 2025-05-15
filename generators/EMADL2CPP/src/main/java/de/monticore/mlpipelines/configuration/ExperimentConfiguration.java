package de.monticore.mlpipelines.configuration;

public class ExperimentConfiguration {

    private final String pathToTrainingConfiguration;

    private final String pathToPipelineImplementations;

    private final String pathToExecutionScript;
    private final String pathForGeneratedBackendArtefacts;

    public ExperimentConfiguration(
            final String pathToTrainingConfiguration,
            final String pathToPipelineImplementations,
            final String pathToExecutionScript,
            final String pathForGeneratedBackendArtefacts) {
        this.pathToTrainingConfiguration = pathToTrainingConfiguration;
        this.pathToPipelineImplementations = pathToPipelineImplementations;
        this.pathToExecutionScript = pathToExecutionScript;
        this.pathForGeneratedBackendArtefacts = pathForGeneratedBackendArtefacts;
    }

    public ExperimentConfiguration(final String pathForGeneratedBackendArtefacts) {
        this.pathToTrainingConfiguration = "";
        this.pathToPipelineImplementations = "";
        this.pathToExecutionScript = "";
        this.pathForGeneratedBackendArtefacts = pathForGeneratedBackendArtefacts;
    }

    public String getPathToTrainingConfiguration() {
        return pathToTrainingConfiguration;
    }

    public String getPathToPipelineImplementations() {
        return pathToPipelineImplementations;
    }

    public String getPathToExecutionScript() {
        return pathToExecutionScript;
    }

    public String getGenerationTargetPath() {
        return pathForGeneratedBackendArtefacts;
    }
}
