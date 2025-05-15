package de.monticore.mlpipelines.backend.generation;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.lang.monticar.cnnarch.generator.generation.ConfLangGenerator;
import de.monticore.lang.monticar.cnnarch.generator.transformation.TemplateLinker;
import de.monticore.lang.monticar.emadl.generator.EMADLGeneratorCli;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;

/***
 * A generator that shall replace EMADLGenerator
 *
 */
public class MontiAnnaGenerator {

    private final MontiAnnaContext montiAnnaContext;

    private final ConfLangGenerator configurationGenerator;

    public MontiAnnaGenerator(final MontiAnnaContext montiAnnaContext) {
        this.montiAnnaContext = montiAnnaContext;
        configurationGenerator = new ConfLangGenerator();
    }

    //TODO FMU break this down gradually with calls in the EMADLGenerator
    public void generateTargetBackendArtefacts() {
        String[] args = {"-m", montiAnnaContext.getParentModelPath().toString(), "-r", montiAnnaContext.getRootModelName(),
                "-o", montiAnnaContext.getExperimentConfiguration().getGenerationTargetPath(),
                "-b", montiAnnaContext.getTargetBackend().toString(), "-f", "n", "-c", "n"};
        EMADLGeneratorCli.main(args);
    }

    public void generateTrainingConfiguration(
            final ASTConfLangCompilationUnit configurationModel,
            final String generatedConfigurationName) {
        new TemplateLinker().linkToTrainingConfigurationTemplates(configurationModel);
        configurationGenerator.generatePythonTrainingConfiguration(configurationModel, generatedConfigurationName);
    }
}
