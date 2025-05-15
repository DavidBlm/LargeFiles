package de.monticore.mlpipelines.automl.trainalgorithms;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;
import de.monticore.mlpipelines.automl.helper.ASTConfLangCompilationUnitHandler;
import de.monticore.mlpipelines.automl.helper.NasAlgorithm;
import de.monticore.mlpipelines.automl.helper.TrainConfigKeys;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.AdaNetAlgorithm;
import de.monticore.mlpipelines.automl.trainalgorithms.efficientnet.EfficientNet;
import de.monticore.mlpipelines.pipelines.Pipeline;
import de.monticore.mlpipelines.pipelines.PythonPipeline;


public class NeuralArchitectureSearchBuilder {
    private final String PytorchPipeline = "Pytorch";

    private ASTConfLangCompilationUnit config;
    private Pipeline trainingPipeline;

    public void setConfig(ASTConfLangCompilationUnit config) {
        this.config = config;
    }

    public void setTrainingPipeline(Pipeline trainingPipeline) {
        this.trainingPipeline = trainingPipeline;
    }

    public NeuralArchitectureSearch build() {
        NeuralArchitectureSearch neuralArchitectureSearch = getNasAlgorithm();
        neuralArchitectureSearch.setTrainPipeline(trainingPipeline);
        neuralArchitectureSearch.setTrainConfiguration(config);
        return neuralArchitectureSearch;
    }

    private NeuralArchitectureSearch getNasAlgorithm() {
        String nasAlgorithmName = getNasAlgorithmName();
        switch (nasAlgorithmName) {
            case NasAlgorithm.EfficientNet:
                return new EfficientNet();
            case NasAlgorithm.AdaNet:
                return new AdaNetAlgorithm();
            default:
                throw new IllegalArgumentException(
                        "Train algorithm " + nasAlgorithmName + " not supported");
        }
    }

    private String getNasAlgorithmName() {
        return (String) ASTConfLangCompilationUnitHandler.getValueByKey(config, TrainConfigKeys.NasAlgorithm);
    }

    private Pipeline getPipeline() {
        String nasAlgorithmName = getNasAlgorithmName();
        if (PytorchPipeline.equals(nasAlgorithmName)) {
            return new PythonPipeline(LearningMethod.SUPERVISED);
        }
        throw new IllegalArgumentException("Pipeline " + nasAlgorithmName + " not supported");
    }
}
