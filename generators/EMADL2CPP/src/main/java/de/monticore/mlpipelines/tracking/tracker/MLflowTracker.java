package de.monticore.mlpipelines.tracking.tracker;

import conflang._ast.ASTConfigurationEntry;
import conflang._ast.ASTNestedConfigurationEntry;
import conflangliterals._ast.ASTListLiteral;
import de.monticore.mcliterals._ast.ASTSignedLiteral;
import de.monticore.mcliterals._ast.ASTStringLiteral;
import de.monticore.mlpipelines.tracking.helper.ASTConfLangHelper;
import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.mlflow.api.proto.Service.CreateRun;
import org.mlflow.api.proto.Service.Experiment;
import org.mlflow.api.proto.Service.Run;
import org.mlflow.api.proto.Service.RunInfo;
import org.mlflow.api.proto.Service.RunStatus;
import org.mlflow.api.proto.Service.ViewType;
import org.mlflow.tracking.MlflowClient;
import org.mlflow.tracking.RunsPage;

/**
 * A {@link RunTracker} that uses the <a href="https://mlflow.org/">MLflow</a> backend to track runs.
 */
public class MLflowTracker implements RunTracker {

    private String mlflowTrackingUri;
    private String experimentName;
    private List<RunStatus> cleanUpStates;

    // Thread-safe setup of experiment
    private static final Semaphore semaphore = new Semaphore(1);
    private static final AtomicInteger nextAvailableRunNumber = new AtomicInteger(1);
    private static boolean setupDone = false;
    private static String experimentID;


    private MlflowClient client;
    private RunInfo currentRun;

    public MLflowTracker(ASTConfigurationEntry config) {
        applyConfig(config);
        setupExperiment();
    }

    private void applyConfig(ASTConfigurationEntry config) {
        if(config == null) {
            Log.error("MLflow tracking requires an entry in the configuration file, but none was provided");
            return;
        }
        if(!(config instanceof ASTNestedConfigurationEntry)) {
            Log.error("MLflow tracking requires a nested configuration entry, but got: " + config.getClass().getSimpleName());
            return;
        }
        ASTNestedConfigurationEntry nestedConfig = (ASTNestedConfigurationEntry) config;

        this.experimentName = ASTConfLangHelper.getSignedLiteralValue(config.getValue());
        this.mlflowTrackingUri = ASTConfLangHelper.getStringValueByKey(nestedConfig, "tracking_uri");
        this.client = new MlflowClient(mlflowTrackingUri);


        // Get list of statuses that runs need to be in, in order to be deleted by cleanUpRepository()
        ASTConfigurationEntry cleanUpConfigurationEntry = ASTConfLangHelper.getConfigurationEntryByKey(nestedConfig, "clean_up");
        if(cleanUpConfigurationEntry == null) {
            // clean_up key not specified: Delete no runs
            cleanUpStates = new ArrayList<>();
        } else {
            ASTSignedLiteral literal = cleanUpConfigurationEntry.getValue();

            if(literal instanceof ASTStringLiteral) {
                // only one status specified
                cleanUpStates = Collections.singletonList(RunStatus.valueOf(ASTConfLangHelper.getSignedLiteralValue(literal).toUpperCase()));
            } else if(literal instanceof ASTListLiteral){
                // list of states specified
                cleanUpStates = ((ASTListLiteral) literal).getSignedLiteralList().stream()
                        .map(l -> RunStatus.valueOf(ASTConfLangHelper.getSignedLiteralValue(l).toUpperCase()))
                        .collect(Collectors.toList());
            }
        }
    }

    /**
     * Sets up the experiment in a thread-safe manner, ensuring that the experiment is only created once.
     */
    private void setupExperiment() {
        try {
            semaphore.acquire();
            if(!setupDone) {
                // Create experiment if it does not exist
                Optional<Experiment> experiment = client.getExperimentByName(experimentName);
                if (experiment.isPresent()) {
                    experimentID = experiment.get().getExperimentId();
                } else {
                    experimentID = client.createExperiment(experimentName);
                }

                // Set next available run number
                RunsPage runsPage = client.searchRuns(Collections.singletonList(experimentID), "", ViewType.ACTIVE_ONLY, 1,
                        Collections.singletonList("attributes.start_time DESC"));
                if(!runsPage.getItems().isEmpty()) {
                    nextAvailableRunNumber.set(Integer.parseInt(runsPage.getItems().get(0).getInfo().getRunName().split(" ")[1]) + 1);
                }

                setupDone = true;
            }
        } catch (InterruptedException e) {
            Log.error("Error while setting up experiment: " + e.getMessage());
        } finally {
            semaphore.release();
        }
    }

    @Override
    public Map<String, String> getPythonParams() {
        if(!runActive()) {
            Log.error("Cannot get python params: No active run");
            return Collections.emptyMap();
        }
        Map<String, String> params = new HashMap<>();
        params.put("mlflow_tracking_uri", mlflowTrackingUri);
        params.put("mlflow_run_id", currentRun.getRunId());
        return params;
    }

    @Override
    public void cleanUpRepository() {
        // MLflow's search does not support OR conditions, so we need to search for each status separately
        for(RunStatus runStatus : cleanUpStates) {
            RunsPage runsPage = client.searchRuns(Collections.singletonList(experimentID),
                    "attributes.status = '" + runStatus.name() + "'",
                    ViewType.ACTIVE_ONLY,
                    10);

            // Deletion cannot be done while iterating over the runsPage
            List<String> runsToDelete = new LinkedList<>();
            do {
                for(Run run : runsPage.getItems()) {
                    runsToDelete.add(run.getInfo().getRunId());
                }
                if (runsPage.hasNextPage()) {
                    runsPage = (RunsPage) runsPage.getNextPage();
                }
            } while(runsPage.hasNextPage());

            // Finally, delete all matching runs
            for(String runId : runsToDelete) {
                client.deleteRun(runId);
            }
        }
    }

    public boolean runActive() {
        return currentRun != null;
    }

    @Override
    public void close() {
        if(runActive())
            client.setTerminated(currentRun.getRunId(), RunStatus.KILLED);
        client.close();
    }

    @Override
    public void startNewRun() {
        if(runActive())
            client.setTerminated(currentRun.getRunId(), RunStatus.KILLED);

        CreateRun run = CreateRun.newBuilder()
                .setRunName("Run " + nextAvailableRunNumber.getAndIncrement())
                .setExperimentId(experimentID)
                .setStartTime(System.currentTimeMillis())
                .build();
        currentRun = client.createRun(run);
    }

    @Override
    public void endRun() {
        if(runActive())
            client.setTerminated(currentRun.getRunId(), RunStatus.FINISHED);
        currentRun = null;
    }

    @Override
    public void logParam(String key, String value) {
        if (!runActive()) {
            Log.error("Logging of parameters " + key + " = " + value + " failed: No active run");
            return;
        }
        client.logParam(currentRun.getRunId(), key, value);
    }

    @Override
    public void logMetric(String key, double value, long timestamp, int step) {
        if (!runActive()) {
            Log.error("Logging of metric " + key + " = " + value + " failed: No active run");
            return;
        }
        client.logMetric(currentRun.getRunId(), key, value, timestamp, step);
    }

    @Override
    public void logArtifact(File file, String artifactPath) {
        if (!runActive()) {
            Log.error("Logging of artifact " + file.getAbsolutePath() + " failed: No active run");
            return;
        }
        if(!file.exists()) {
            Log.error("Logging of artifact " + file.getAbsolutePath() + " failed: "+ (file.isDirectory() ? "Directory" : "File") +" does not exist");
            return;
        }

        if(file.isDirectory()) {
            client.logArtifacts(currentRun.getRunId(), file, artifactPath);
            return;
        }
        client.logArtifact(currentRun.getRunId(), file, artifactPath);
    }

    @Override
    public void logTag(String key, String value) {
        if (!runActive()) {
            Log.error("Logging of tag " + key + " = " + value + " failed: No active run");
            return;
        }
        client.setTag(currentRun.getRunId(), key, value);
    }

}
