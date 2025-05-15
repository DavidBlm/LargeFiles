package de.monticore.mlpipelines.automl.trainalgorithms;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.pipelines.Pipeline;
import de.monticore.mlpipelines.tracking.tracker.MultiBackendTracker;

public abstract class NeuralArchitectureSearch {
    public double trainedAccuracy;
    private ASTConfLangCompilationUnit trainConfiguration;
    private Pipeline trainPipeline;
    private ArchitectureSymbol startNetwork;

    public abstract ArchitectureSymbol execute(ArchitectureSymbol startNetwork);

    public <T extends ASTConfLangCompilationUnit> T getTrainConfiguration() {
        return (T) trainConfiguration;
    }

    public void setTrainConfiguration(ASTConfLangCompilationUnit trainConfiguration) {
        this.trainConfiguration = trainConfiguration;
    }

    public void setTrainPipeline(Pipeline trainPipeline) {
        this.trainPipeline = trainPipeline;
    }

    public void setStartNetwork(ArchitectureSymbol startNetwork) {
        this.startNetwork = startNetwork;
    }

    public Pipeline getTrainPipeline() {
        return trainPipeline;
    }

    public ArchitectureSymbol getStartNetwork() {
        return startNetwork;
    }

    public MultiBackendTracker getRunTracker() {
        return trainPipeline.getRunTracker();
    }
}
