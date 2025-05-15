package de.monticore.mlpipelines.workflow;

import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;
import de.monticore.mlpipelines.pipelines.PythonPipeline;
import de.monticore.mlpipelines.pipelines.executor.MontiAnnaPipelineExecutor;

public class MontiAnnaPipelineWorkflow extends AbstractWorkflow {
    public MontiAnnaPipelineWorkflow(final MontiAnnaContext applicationContext) {
        super(applicationContext);
    }

    public void createPipelineExecutor(final LearningMethod learningMethod) {
        final PythonPipeline pythonPipeline = new PythonPipeline(learningMethod);
        pythonPipeline.setMontiAnnaGenerator(this.montiAnnaGenerator);

        final MontiAnnaPipelineExecutor montiAnnaPipelineExecutor = new MontiAnnaPipelineExecutor();
        montiAnnaPipelineExecutor.setTrainPipeline(pythonPipeline);

        this.setPipelineExecutor(montiAnnaPipelineExecutor);
    }
}
