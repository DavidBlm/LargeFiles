package de.monticore.mlpipelines.automl.configuration;

public abstract class TrainAlgorithmConfig {
    //private class TrainAlgorithm;
    public boolean saveTrainedArchitecture;
    public String architectureSavePath;
    public String trainAlgorithmName;
    private String trainPipelineName;

    public TrainAlgorithmConfig() {
    }

    public TrainAlgorithmConfig(
            boolean saveTrainedArchitecture,
            String architectureSavePath,
            String trainAlgorithmName,
            String trainPipelineName) {
        this.saveTrainedArchitecture = saveTrainedArchitecture;
        this.architectureSavePath = architectureSavePath;
        this.trainAlgorithmName = trainAlgorithmName;
        this.trainPipelineName = trainPipelineName;
    }

    public boolean isSaveTrainedArchitecture() {
        return saveTrainedArchitecture;
    }

    public void setSaveTrainedArchitecture(boolean saveTrainedArchitecture) {
        this.saveTrainedArchitecture = saveTrainedArchitecture;
    }

    public String getArchitectureSavePath() {
        return architectureSavePath;
    }

    public void setArchitectureSavePath(String architectureSavePath) {
        this.architectureSavePath = architectureSavePath;
    }

    public String getTrainAlgorithmName() {
        return trainAlgorithmName;
    }

    public void setTrainAlgorithmName(String trainAlgorithmName) {
        this.trainAlgorithmName = trainAlgorithmName;
    }

    public String getTrainPipelineName() {
        return trainPipelineName;
    }

    public void setTrainPipelineName(String pipelineName) {
        this.trainPipelineName = pipelineName;
    }
}
