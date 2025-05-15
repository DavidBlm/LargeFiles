package de.monticore.mlpipelines.workflow;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;
import de.monticore.mlpipelines.automl.helper.ConfigurationValidationHandler;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;
import de.monticore.mlpipelines.pipelines.PythonPipeline;
import de.monticore.mlpipelines.pipelines.executor.AutoMLPipelineExecutor;
import java.io.IOException;
import java.util.Map;

public class AutoMLPipelineWorkflow extends AbstractWorkflow {
    public AutoMLPipelineWorkflow(final MontiAnnaContext applicationContext) {
        super(applicationContext);
    }

    public void createPipelineExecutor(final LearningMethod learningMethod) {
        final PythonPipeline pythonPipeline = new PythonPipeline(learningMethod);
        pythonPipeline.setMontiAnnaGenerator(this.montiAnnaGenerator);

        final AutoMLPipelineExecutor autoMLPipelineExecutor = new AutoMLPipelineExecutor();
        autoMLPipelineExecutor.setTrainPipeline(pythonPipeline);

        this.setPipelineExecutor(autoMLPipelineExecutor);
    }

    @Override
    protected void addWorkflowSpecificConfigs(Map<String, Object> configMap,
            String instanceName,
            String componentTypeName,
            String pathToModelsDirectory,
            String rootModelName) throws IOException {

        // Neural Architecture Search:
        ASTConfLangCompilationUnit nasConf = this.getNASConfiguration(
                pathToModelsDirectory, rootModelName, instanceName, componentTypeName
        );
        ConfigurationValidationHandler.validateConfiguration(nasConf, this.pipelineExecutor.getSchemasTargetDir());
        configMap.put("nasConf", nasConf);

        if (nasConf != null) {
            ASTConfLangCompilationUnit instanceTrainingConfiguration = (ASTConfLangCompilationUnit) configMap.get("trainingConfiguration");
            instanceTrainingConfiguration.getConfiguration().addSuperConfiguration(nasConf.getConfiguration());
        }

        // Hyperparameter Optimization:
        ASTConfLangCompilationUnit searchSpace = this.getInstanceConfiguration(
                pathToModelsDirectory, rootModelName, instanceName, componentTypeName, "SearchSpace.conf"
        );
        ASTConfLangCompilationUnit hyperparamsOptConf = this.getInstanceConfiguration(
                pathToModelsDirectory, rootModelName, instanceName, componentTypeName, "HyperparameterOpt.conf"
        );
        ASTConfLangCompilationUnit evaluationCriteria = this.getInstanceConfiguration(
                pathToModelsDirectory, rootModelName, instanceName, componentTypeName, "EvaluationCriteria.conf"
        );
        ConfigurationValidationHandler.validateConfiguration(evaluationCriteria, this.pipelineExecutor.getSchemasTargetDir());
        configMap.put("SearchSpace", searchSpace);
        configMap.put("HyperparameterOpt", hyperparamsOptConf);
        configMap.put("EvaluationCriteria", evaluationCriteria);
    }

    protected ASTConfLangCompilationUnit getNASConfiguration(
            String pathToModelsDirectory, String rootModelName,
            String instanceName, String componentTypeName) throws IOException {
        switch (componentTypeName) {
            case "AdaNetCustom":
                return this.getInstanceConfiguration(
                        pathToModelsDirectory, rootModelName, instanceName, componentTypeName, "AdaNet.conf");
            case "EfficientNetBase":
                return this.getInstanceConfiguration(
                        pathToModelsDirectory, rootModelName, instanceName, componentTypeName, "EfficientNet.conf");
            default:
                return null;
        }
    }

}
