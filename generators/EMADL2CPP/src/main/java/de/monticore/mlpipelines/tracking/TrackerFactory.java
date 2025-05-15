package de.monticore.mlpipelines.tracking;

import conflang._ast.ASTConfLangCompilationUnit;
import conflang._ast.ASTConfigurationEntry;
import conflangliterals._ast.ASTListLiteral;
import de.monticore.mcliterals._ast.ASTSignedLiteral;
import de.monticore.mlpipelines.tracking.helper.ASTConfLangHelper;
import de.monticore.mlpipelines.tracking.helper.SimpleRegexMatcher;
import de.monticore.mlpipelines.tracking.tracker.MLflowTracker;
import de.monticore.mlpipelines.tracking.tracker.MultiBackendTracker;
import de.monticore.mlpipelines.tracking.tracker.RunTracker;
import de.monticore.mlpipelines.tracking.tracker.StdOutTracker;
import de.monticore.parsing.ConfigurationLanguageParser;
import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TrackerFactory {

    private final ASTConfLangCompilationUnit trackingConfig;

    public TrackerFactory(String trackingConfigurationFilePath) {
        if(trackingConfigurationFilePath == null) {
            // No config path set. Tracking will be disabled.
            Log.info("No tracking configuration file set. Tracking will be disabled.", TrackerFactory.class.getName());
            this.trackingConfig = null;
            return;
        }

        File trackingConfigFile = new File(trackingConfigurationFilePath);
        if(!trackingConfigFile.exists() || !trackingConfigFile.isFile()) {
            throw new IllegalArgumentException("Tracking config does not exist or is a directory: " + trackingConfigFile);
        }
        this.trackingConfig = loadTrackingConfiguration(trackingConfigFile);
    }

    public MultiBackendTracker createTracker() {
        if(isTrackingDisabled()) {
            return new MultiBackendTracker(new ArrayList<>(), new ArrayList<>(), new HashMap<>(), new SimpleRegexMatcher());
        }

        List<TrackingBackend> enabledTrackingBackends = extractEnabledTrackingBackends();

        // Get configuration and create tracker for each enabled backend
        List<RunTracker> trackers = enabledTrackingBackends.stream().map(backend -> {
            ASTConfigurationEntry backendConfig = ASTConfLangHelper.getConfigurationEntryByKey(trackingConfig, backend.name().toLowerCase());
            switch (backend) {
                case MLFLOW:
                    return new MLflowTracker(backendConfig);
                case STDOUT:
                    return new StdOutTracker(backendConfig);
                default:
                    throw new IllegalArgumentException("Unknown tracking backend: " + backend);
            }
        }).collect(Collectors.toList());

        // Create MultiBackendTracker with RunTrackers, tags and blacklist
        return new MultiBackendTracker(trackers, enabledTrackingBackends, extractTags(), new SimpleRegexMatcher(extractParamBlacklist()));
    }

    private ASTConfLangCompilationUnit loadTrackingConfiguration(File trackingConfigFile) {
        try {
            return new ConfigurationLanguageParser().parseModelIfExists(trackingConfigFile.getAbsolutePath());
        } catch (IOException e) {
            Log.error("Failed to load tracking configuration file: " + trackingConfigFile, e);
        }
        return null;
    }

    private List<TrackingBackend> extractEnabledTrackingBackends() {
        List<TrackingBackend> res = new LinkedList<>();

        // Check, if there is a key named after each tracking backend
        for(TrackingBackend backend : TrackingBackend.values()) {
            ASTConfigurationEntry backendConfig = ASTConfLangHelper.getConfigurationEntryByKey(trackingConfig, backend.name().toLowerCase());
            if(backendConfig != null) {
                res.add(backend);
            }
        }

        return res;
    }

    private Map<String, String> extractTags() {
        Map<String, String> res = new HashMap<>();
        ASTConfigurationEntry configurationEntry = ASTConfLangHelper.getConfigurationEntryByKey(trackingConfig, "tags");
        if(configurationEntry == null) {
            return res;
        }

        if(!(configurationEntry.getValue() instanceof ASTListLiteral)) {
            Log.error("Value of key 'tags' in tracking config must be a list.");
            return res;
        }

        ASTListLiteral listLiteral = (ASTListLiteral) configurationEntry.getValue();
        for(ASTSignedLiteral literal : listLiteral.getSignedLiteralList()) {
            if(!(literal instanceof ASTListLiteral)) {
                Log.error("Value of key 'tags' in tracking config must be a list of lists.");
                return res;
            }

            ASTListLiteral innerListLiteral = (ASTListLiteral) literal;
            if(innerListLiteral.sizeSignedLiterals() != 2) {
                Log.error("Value of key 'tags' in tracking config must be a list of lists with two elements.");
                return res;
            }

            String key = ASTConfLangHelper.getSignedLiteralValue(innerListLiteral.getSignedLiteral(0));
            String value = ASTConfLangHelper.getSignedLiteralValue(innerListLiteral.getSignedLiteral(1));

            res.put(key, value);
        }
        return res;
    }

    private List<String> extractParamBlacklist() {
        List<String> res = new ArrayList<>();
        ASTConfigurationEntry configurationEntry = ASTConfLangHelper.getConfigurationEntryByKey(trackingConfig, "param_blacklist");
        if(configurationEntry == null) {
            return res;
        }

        if(!(configurationEntry.getValue() instanceof ASTListLiteral)) {
            Log.error("Value of key 'param_blacklist' in tracking config must be a list.");
            return res;
        }

        ASTListLiteral listLiteral = (ASTListLiteral) configurationEntry.getValue();
        for(ASTSignedLiteral literal : listLiteral.getSignedLiteralList()) {
            res.add(ASTConfLangHelper.getSignedLiteralValue(literal));
        }
        return res;
    }

    private boolean isTrackingDisabled() {
        return trackingConfig == null;
    }

}
