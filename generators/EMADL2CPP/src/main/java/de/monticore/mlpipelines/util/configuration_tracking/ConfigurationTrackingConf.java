package de.monticore.mlpipelines.util.configuration_tracking;

import conflang._ast.ASTConfLangCompilationUnit;
import conflang._ast.ASTConfigurationEntry;
import conflangliterals._ast.ASTListLiteral;
import de.monticore.mlpipelines.tracking.helper.ASTConfLangHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ConfigurationTrackingConf {
    private static final String DATASET_FILE_NAME = "datasetInfo.txt";
    private static final String pathTmp = "target/tmp/configurationTracking";
    private static final String uuid = UUID.randomUUID().toString();
    private static boolean enabled;
    private static String gitlabTrackingUrl;
    private static List<String> mlflowTrackingUrls;
    private static String experimentName;
    private static String modelName;
    private static String modelPath;
    private static String groupId;
    
    public static void fromASTConfLangCompilationUnit(ASTConfLangCompilationUnit configurationTrackingConf) {
        enabled = true;
        ASTConfigurationEntry groupIdEntry = ASTConfLangHelper.getConfigurationEntryByKey(configurationTrackingConf, "group_id");
        ASTConfigurationEntry experimentNameEntry = ASTConfLangHelper.getConfigurationEntryByKey(configurationTrackingConf, "experiment_name");
        ASTConfigurationEntry gitlabTrackingUrlEntry = ASTConfLangHelper.getConfigurationEntryByKey(configurationTrackingConf, "gitlab_tracking_url");
        ASTConfigurationEntry mlflowTrackingUrlEntry = ASTConfLangHelper.getConfigurationEntryByKey(configurationTrackingConf, "mlflow_tracking_urls");
        if (groupIdEntry != null) {
            groupId = ASTConfLangHelper.getSignedLiteralValue(groupIdEntry.getValue());
        }
        if (experimentNameEntry != null) {
            experimentName = ASTConfLangHelper.getSignedLiteralValue(experimentNameEntry.getValue());
        }
        if (gitlabTrackingUrlEntry != null) {
            gitlabTrackingUrl = ASTConfLangHelper.getSignedLiteralValue(gitlabTrackingUrlEntry.getValue());
        }
        if (mlflowTrackingUrlEntry != null) {
            mlflowTrackingUrls = ((ASTListLiteral) mlflowTrackingUrlEntry.getValue()).getSignedLiteralList().stream()
                    .map(ASTConfLangHelper::getSignedLiteralValue)
                    .collect(Collectors.toList());
        }
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static List<String> getMlflowTrackingUrls() {
        return mlflowTrackingUrls;
    }

    public static String getPathTmp() {
        return pathTmp;
    }

    public static String getExperimentName() {
        if (experimentName != null && !experimentName.isEmpty()) {
            return experimentName;
        }
        return uuid;
    }

    public static String getGroupId() {
        return groupId;
    }

    public static String getModelName() {
        return modelName;
    }

    public static String getModelPath() {
        return modelPath;
    }

    public static void setModelName(String modelName_) {
        modelName = modelName_;
    }

    public static void setModelPath(String modelPath_) {
        modelPath = modelPath_;
    }

    public static String getGitlabTrackingUrl() {
        return gitlabTrackingUrl;
    }

    public static String getGitlabArtifactsPath() {
        return String.format("%s/gitlabArtifacts", getPathTmp());
    }

    public static String getDatasetFileName() {
        return DATASET_FILE_NAME;
    }
}
