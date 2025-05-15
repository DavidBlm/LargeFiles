package de.monticore.mlpipelines.workflow;

import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;

public class DummyWorkflow extends AbstractWorkflow {
    public DummyWorkflow(final MontiAnnaContext applicationContext) {
        super(applicationContext);
    }

    public DummyWorkflow() {
        super();
    }

    @Override
    public void createPipelineExecutor(LearningMethod learningMethod) {
    }

}
