package de.monticore.mlpipelines.tracking.tracker;

import conflang._ast.ASTConfigurationEntry;
import de.monticore.mlpipelines.tracking.helper.ASTConfLangHelper;
import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * A {@link RunTracker} that logs all calls to stdout if info is enabled.
 */
public class StdOutTracker implements RunTracker {

    private final String experimentName;

    public StdOutTracker(ASTConfigurationEntry config) {
        experimentName = ASTConfLangHelper.getSignedLiteralValue(config.getValue());
    }


    @Override
    public void close() {
        Log.info("StdOutTracker closing!", this.getClass().getName());
    }

    @Override
    public void startNewRun() {
        Log.info("StdOutTracker starting new run!", this.getClass().getName());
    }

    @Override
    public void endRun() {
        Log.info("StdOutTracker ending run!", this.getClass().getName());
    }

    @Override
    public void logParam(String key, String value) {
        Log.info("StdOutTracker logging param: " + key + "=" + value, this.getClass().getName());
    }

    @Override
    public void logArtifact(File file, String artifactPath) {
        Log.info("StdOutTracker logging artifact: " + file.getAbsolutePath() + " into directory: "
                + artifactPath, this.getClass().getName());
    }

    @Override
    public void logMetric(String key, double value, long timestamp, int step) {
        Log.info("StdOutTracker logging metric: " + key + "=" + value + " at timestamp " + timestamp + "(step=" + step + ")", this.getClass().getName());
    }

    @Override
    public void logTag(String key, String value) {
        Log.info("StdOutTracker setting tag: " + key + "=" + value, this.getClass().getName());
    }

    @Override
    public void cleanUpRepository() {
        Log.info("StdOutTracker cleaning up repository!", this.getClass().getName());
    }

    @Override
    public Map<String, String> getPythonParams() {
        Map<String, String> res = new HashMap<>();
        res.put("stdout_experiment_name", experimentName);
        return res;
    }

}
