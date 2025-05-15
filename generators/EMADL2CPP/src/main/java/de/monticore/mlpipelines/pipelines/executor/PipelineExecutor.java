package de.monticore.mlpipelines.pipelines.executor;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;
import de.monticore.mlpipelines.pipelines.Pipeline;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class PipelineExecutor {

    protected Pipeline trainPipeline;
    protected List<Map<String, Object>> networkExecutionConfigs;
    protected final List<File> emadlFiles = new ArrayList<>();
    protected MontiAnnaContext montiAnnaContext;

    protected String schemasTargetDir;
    protected EMAComponentInstanceSymbol pipelineModelWithExecutionSemantics;

    public abstract void execute();

    /**
     * Applies the basic configuration necessary for execution of the pipeline, given the index of the network to be executed
     * @param networkIndex The index of the network inside the list of network execution configurations
     */
    public void applyBasicConfiguration(int networkIndex) {
        Map<String, Object> configMap = this.networkExecutionConfigs.get(networkIndex);
        trainPipeline.setNeuralNetwork((EMAComponentInstanceSymbol) configMap.get("network"));
        trainPipeline.setNetworkName((String) configMap.get("networkName"));
        trainPipeline.setTrainingConfiguration((ASTConfLangCompilationUnit) configMap.get("trainingConfiguration"));
        trainPipeline.setPipelineConfiguration((ASTConfLangCompilationUnit) configMap.get("pipelineConfiguration"));

        trainPipeline.setSchemasTargetDir(schemasTargetDir);
        trainPipeline.setPipelineModelWithExecutionSemantics(pipelineModelWithExecutionSemantics);
    }

    public void addEmadlFile(File emadlFile) {
        this.emadlFiles.add(emadlFile);
    }

    public void addEmadlFiles(List<File> emadlFiles) {
        this.emadlFiles.addAll(emadlFiles);
    }

    public MontiAnnaContext getMontiAnnaContext() {
        return montiAnnaContext;
    }

    public void setMontiAnnaContext(MontiAnnaContext montiAnnaContext) {
        this.montiAnnaContext = montiAnnaContext;
    }

    public Pipeline getTrainPipeline() {
        return trainPipeline;
    }

    public void setTrainPipeline(Pipeline trainPipeline) {
        this.trainPipeline = trainPipeline;
    }

    public void setNetworkExecutionConfigs(List<Map<String, Object>> networkExecutionConfigs) {
        this.networkExecutionConfigs = networkExecutionConfigs;
    }

    public void setSchemasTargetDir(String schemasTargetDir) {
        this.schemasTargetDir = schemasTargetDir;
    }

    public String getSchemasTargetDir() {
        return schemasTargetDir;
    }

    public void setPipelineModelWithExecutionSemantics(
            EMAComponentInstanceSymbol pipelineModelWithExecutionSemantics) {
        this.pipelineModelWithExecutionSemantics = pipelineModelWithExecutionSemantics;
    }
}
