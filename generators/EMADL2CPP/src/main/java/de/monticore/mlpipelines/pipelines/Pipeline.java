package de.monticore.mlpipelines.pipelines;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;
import de.monticore.mlpipelines.automl.emadlprinter.EmadlPrettyPrinter;
import de.monticore.mlpipelines.tracking.tracker.MultiBackendTracker;

public abstract class Pipeline {

    protected final LearningMethod learningMethod;

    protected ASTConfLangCompilationUnit trainingConfiguration;

    protected ASTConfLangCompilationUnit pipelineConfiguration;

    protected EMAComponentInstanceSymbol neuralNetwork;

    private String networkName;

    protected EMAComponentInstanceSymbol pipelineModelWithExecutionSemantics;

    protected String modelOutputDirectory = "/model/mnist.LeNetNetwork/";

    protected String modelOutputDirectoryWithDot = "." + modelOutputDirectory;

    protected String schemasTargetDir;

    protected MultiBackendTracker runTracker;

    protected Pipeline(final LearningMethod learningMethod) {
        this.learningMethod = learningMethod;
    }

    public String getPrettyPrintedNetwork() {
        if(neuralNetwork == null) {
            return "";
        }
        return new EmadlPrettyPrinter().prettyPrint((ArchitectureSymbol) neuralNetwork.getSpannedScope().getSubScopes().get(0).getSpanningSymbol().get());
    }

    public void setNeuralNetwork(final EMAComponentInstanceSymbol neuralNetwork) {
        this.neuralNetwork = neuralNetwork;
    }

    public EMAComponentInstanceSymbol getNeuralNetwork() {
        return neuralNetwork;
    }

    public void setTrainingConfiguration(final ASTConfLangCompilationUnit trainingConfiguration) {
        this.trainingConfiguration = trainingConfiguration;
    }

    public ASTConfLangCompilationUnit getTrainingConfiguration() {
        return trainingConfiguration;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public void setPipelineModelWithExecutionSemantics(final EMAComponentInstanceSymbol pipelineModelWithExecutionSemantics) {
        this.pipelineModelWithExecutionSemantics = pipelineModelWithExecutionSemantics;
    }

    public abstract void execute();

    public void setPipelineConfiguration(final ASTConfLangCompilationUnit pipelineConfiguration) {
        this.pipelineConfiguration = pipelineConfiguration;
    }

    public ASTConfLangCompilationUnit getPipelineConfiguration() {
        return pipelineConfiguration;
    }

    public void setModelOutputDirectory(final String modelOutputDirectory) {
        this.modelOutputDirectory = modelOutputDirectory;
        this.modelOutputDirectoryWithDot = "." + modelOutputDirectory;
    }

    public String getSchemasTargetDir() {
        return schemasTargetDir;
    }

    public void setRunTracker(MultiBackendTracker runTracker) {
        this.runTracker = runTracker;
    }

    public MultiBackendTracker getRunTracker() {
        return runTracker;
    }

    public void setSchemasTargetDir(String schemasTargetDir) {
        this.schemasTargetDir = schemasTargetDir;
    }

    public abstract float getTrainedAccuracy();
}
