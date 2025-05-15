package de.monticore.mlpipelines.pipelines;

import com.google.common.collect.Lists;
import conflang._ast.ASTConfigurationEntry;
import de.monticore.lang.monticar.cnnarch.generator.training.LearningMethod;
import de.monticore.mlpipelines.backend.generation.MontiAnnaGenerator;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;
import de.monticore.montipipes.config.ExecutionScriptConfiguration;
import de.monticore.montipipes.generators.PipelineGenerator;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;
import org.json.JSONObject;

public class PythonPipeline extends Pipeline {
    private MontiAnnaGenerator montiAnnaGenerator;

    public PythonPipeline(final LearningMethod learningMethod) {
        super(learningMethod);
    }

    public void setMontiAnnaGenerator(final MontiAnnaGenerator montiAnnaGenerator) {
        this.montiAnnaGenerator = montiAnnaGenerator;
    }

    @Override
    public void execute() {
        generateTrainingConfiguration();
        generatePipelineExecutionScript();
        Process process = runScript();
        final InputStream pythonConsoleStream = process.getInputStream();
        try {
            String result = IOUtils.toString(pythonConsoleStream, StandardCharsets.UTF_8);
            process.waitFor();
            System.out.println(result);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public float getTrainedAccuracy() {
        JSONObject json = getTrainingResultsAsJsonObject();
        return json.getFloat("accuracy");
    }

    private JSONObject getTrainingResultsAsJsonObject() {
        String generatedModelPath = MontiAnnaContext.getInstance()
                .getExperimentConfiguration()
                .getPathToExecutionScript();
        String pathToOutputFile = generatedModelPath + modelOutputDirectory;
        String fileName = "results.json";
        String pathToFile;
        if (SystemUtils.IS_OS_WINDOWS) {
            pathToFile = System.getProperty("user.dir") + "\\" + pathToOutputFile + fileName;
        } else {
            pathToFile = System.getProperty("user.dir") + "/" + pathToOutputFile + fileName;
        }

        JSONObject json;
        try {
            FileReader reader = new FileReader(pathToFile);
            json = new JSONObject(IOUtils.toString(reader));
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return json;
    }

    private static String getTrainingOutputFileName(String pathToOutputFile) {
        File folder = new File(pathToOutputFile);
        File[] listOfFiles = folder.listFiles();
        String fileName = "";
        for (File file : listOfFiles) {
            if (file.isFile() && file.getName().startsWith("model_") && file.getName().endsWith(".json")) {
                fileName = file.getName();
                break;
            }
        }
        return fileName;
    }

    public void generateTrainingConfiguration() {
        final String configName = "Training_Configuration_" + this.getNetworkName();
        montiAnnaGenerator.generateTrainingConfiguration(this.trainingConfiguration, configName);
    }

    public void generatePipelineExecutionScript() {
        final List<ASTConfigurationEntry> pipelineConfigurationEntries = pipelineConfiguration.getConfiguration()
                .getAllConfigurationEntries();
        final String trainingConfFileName = calculatePathToGeneratedTrainingConfiguration().getFileName().toString();
        final String generatedNetworkFileName = calculatePathToGeneratedNetwork().getFileName().toString();

        final String schemaAPIScriptName = createSchemaApiPathFromLearningMethod().getFileName().toString();
        final List<String> scriptDependencies = Lists.newArrayList(schemaAPIScriptName, trainingConfFileName,
                generatedNetworkFileName);
        final PipelineGenerator pipelineGenerator = new PipelineGenerator();
        final List<String> cliArguments = new ArrayList<>(runTracker.getPythonParams().keySet());
        pipelineGenerator.setTrainingConfigurationName(trainingConfFileName);
        pipelineGenerator.setSchemaAPIName(schemaAPIScriptName);

        pipelineGenerator.addScriptConfiguration(ExecutionScriptConfiguration.MODEL_DIRECTORY,
                modelOutputDirectoryWithDot);
        pipelineGenerator.generatePipelineExecutor(pipelineConfigurationEntries,
                this.pipelineModelWithExecutionSemantics, scriptDependencies, cliArguments);
    }

    protected Process runScript() {
        final String pathToExecutionScriptDirectory = MontiAnnaContext.getInstance()
                .getExperimentConfiguration()
                .getPathToExecutionScript();
        final String pathToExecutionScript = Paths.get("Pipeline_Executor.py").toString();

        // Build the command
        ProcessBuilder processBuilder = getProcessBuilder(pathToExecutionScript, pathToExecutionScriptDirectory);
        try {
            return processBuilder.start();
        } catch (IOException e) {
            throw new RuntimeException("Pipeline execution has aborted", e);
        }
    }

    private ProcessBuilder getProcessBuilder(String pathToExecutionScript, String pathToExecutionScriptDirectory) {
        List<String> command = new LinkedList<>();
        command.add("python3");
        command.add(pathToExecutionScript);
        for(Entry<String, String> entry : runTracker.getPythonParams().entrySet()) {
            command.add("--" + entry.getKey());
            if(!entry.getValue().isEmpty())
                command.add(entry.getValue());
        }

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        processBuilder.directory(new File(pathToExecutionScriptDirectory));
        return processBuilder;
    }

    private Path calculatePathToGeneratedTrainingConfiguration() {
        return Paths.get(MontiAnnaContext.getInstance().getExperimentConfiguration().getPathToTrainingConfiguration(),
                "Training_Configuration_" + this.getNetworkName());
    }

    private Path calculatePathToGeneratedNetwork() {
        final String NetworkName = this.neuralNetwork.getFullName().replace(".", "_");
        return Paths.get(MontiAnnaContext.getInstance().getExperimentConfiguration().getPathToTrainingConfiguration(),
                "CNNNet_" + NetworkName);
    }

    public Path createSchemaApiPathFromLearningMethod() {
        final String capitalisedLearningMethodName = this.learningMethod.name().charAt(0) + this.learningMethod.name()
                .substring(1)
                .toLowerCase();
        return Paths.get(MontiAnnaContext.getInstance().getExperimentConfiguration().getPathToTrainingConfiguration(),
                capitalisedLearningMethodName + "_Schema_API");
    }
}
