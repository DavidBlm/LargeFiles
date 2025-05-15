package de.monticore.lang.monticar.emadl.generator;

import static de.monticore.lang.monticar.cnnarch.generator.validation.Constants.ROOT_SCHEMA_MODEL_PATH;
import static de.monticore.lang.monticar.generator.cpp.GeneratorCppCli.OPTION_MODELS_PATH;
import static de.monticore.lang.monticar.generator.cpp.GeneratorCppCli.OPTION_ROOT_MODEL;

import de.monticore.mlpipelines.configuration.ExperimentConfiguration;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;
import de.monticore.mlpipelines.tracking.TrackerFactory;
import de.monticore.mlpipelines.util.ResourcesUtil;
import de.monticore.mlpipelines.workflow.AutoMLPipelineWorkflow;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

public class AutoMLCli extends EMADLGeneratorCli {

    public static void main(String[] args) {
        Options options = getOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine cliArgs = parseArgs(options, parser, args);
        if (cliArgs != null) {
            runWorkflow(cliArgs);
        }
    }

    private static void runWorkflow(final CommandLine cliArgs) {
        String rootModelName = cliArgs.getOptionValue(OPTION_ROOT_MODEL.getOpt());
        String backendString = cliArgs.getOptionValue(OPTION_BACKEND.getOpt());
        String trackingConfigurationPath = cliArgs.getOptionValue(OPTION_TRACKING_CONFIGURATION.getOpt());
        Path modelsDirPath = Paths.get(cliArgs.getOptionValue(OPTION_MODELS_PATH.getOpt()));
        final String DEFAULT_BACKEND = "PYTORCH";

        if (backendString == null) {
            Log.warn("Backend not specified. Backend set to default value " + DEFAULT_BACKEND);
            backendString = DEFAULT_BACKEND;
        }

        Optional<Backend> backend = Backend.getBackendFromString(backendString);
        if (!backend.isPresent()) {
            Log.warn(
                    "Specified backend " + backendString + " is not supported. Backend set to default value " + DEFAULT_BACKEND);
            backend = Backend.getBackendFromString(DEFAULT_BACKEND);
        }

        TrackerFactory trackerFactory = new TrackerFactory(trackingConfigurationPath);

        ResourcesUtil.copySchemaFilesFromResource(ROOT_SCHEMA_MODEL_PATH, "target/classes/");
        try {
            if (backend.get().equals(Backend.PYTORCH)) {

                redirectToNewToolchain(rootModelName, modelsDirPath, trackerFactory);
                return;
            }
            runGenerator(cliArgs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void redirectToNewToolchain(
            final String rootModelName,
            final Path modelsDirPath,
            final TrackerFactory trackerFactory) throws IOException {
        Log.warn("Redirecting for PyTorch");
        final ExperimentConfiguration experimentConfiguration = new ExperimentConfiguration(
                "src/main/resources/adanet_experiment/resources/schema_apis",
                "src/main/resources/experiments/steps"
                , "target/generated-sources", "target/generated-sources/backend");
        final MontiAnnaContext montiAnnaContext = MontiAnnaContext.getInstance();
        montiAnnaContext.initContext(modelsDirPath, rootModelName, experimentConfiguration, trackerFactory);
        montiAnnaContext.setPipelineReferenceModelsPath(Paths.get("src/main/resources/pipelines"));
        new AutoMLPipelineWorkflow(montiAnnaContext).execute();
    }
}
