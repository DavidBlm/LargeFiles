package de.monticore.mlpipelines.util.configuration_tracking;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.emadlprinter.ASTConfLangCompilationUnitPrinter;
import de.monticore.mlpipelines.automl.emadlprinter.EmadlPrettyPrinter;
import de.monticore.mlpipelines.pipelines.Pipeline;
import de.se_rwth.commons.logging.Log;
import java.io.FileWriter;
import java.io.IOException;

public class ConfigurationTrackingReport {
    private static final StringBuilder report = new StringBuilder();
    private static final String reportFilePath = String.format("%s/%s/report.txt", ConfigurationTrackingConf.getPathTmp(), ConfigurationTrackingConf.getExperimentName());

    public static void logMessage(String message) {
        report.append("\t").append(message).append("\n");
    }

    public static void logDuplicateArtifact(Artifact artifact) {
        report.append(String.format("\t'%s' from the '%s' experiment has the same configuration and achieved train accuracy %.2f\n", artifact.getInfoValue("run_name"), artifact.getExperimentName(), artifact.getAccuracy()));
    }

    public static void logInitializeCheck(Artifact artifact) {
        report.append("-------------------------------------------------------------------------------------------------").append("\n");
        report.append("\n").append(String.format("Running `%s` from the current experiment (stage = %s):", artifact.getInfoValue("run_name"), artifact.getInfoValue("stage"))).append("\n");
    }

    public static void logPipeline(Pipeline pipeline) {
        float accuracy = ConfigurationTrackingManager.getArtifact().getAccuracy();
        logMessage("The model was successfully trained with the following training configuration:\n");
        logMessage(new ASTConfLangCompilationUnitPrinter().prettyPrint(pipeline.getTrainingConfiguration()));
        logMessage("and the following network architecture:\n");
        logMessage(new EmadlPrettyPrinter().prettyPrint((ArchitectureSymbol) pipeline.getNeuralNetwork().getSpannedScope().getSubScopes().get(0).getSpanningSymbol().get()));
        logMessage(String.format("The model achieved train accuracy: %.2f", accuracy));
    }

    public static void saveReport() {
        try {
            FileWriter reportFile = new FileWriter(reportFilePath);
            reportFile.write(report.toString());
            reportFile.close();
        } catch (IOException e) {
            Log.info("Could not save the report into " + reportFilePath, ConfigurationTrackingReport.class.getName());
        }

    }
}
