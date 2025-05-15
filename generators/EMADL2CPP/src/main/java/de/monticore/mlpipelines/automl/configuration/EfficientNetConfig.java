package de.monticore.mlpipelines.automl.configuration;

import conflang._ast.ASTConfLangCompilationUnit;
import de.monticore.mlpipelines.automl.helper.ASTConfLangCompilationUnitHandler;
import de.monticore.mlpipelines.automl.trainalgorithms.efficientnet.ScalingFactors;

public class EfficientNetConfig {
    private final boolean saveTrainedArchitecture;
    private final String architectureSavePath;
    private final String trainAlgorithmName;
    private final float flopsConditionValue;
    private final ScalingFactors minScalingFactors;
    private final ScalingFactors maxScalingFactors;
    private final ScalingFactors scalingFactorsStepSize;
    private final int phi;
    private final int numClasses;
    private int minImageWidthAndHeight;
    private int maxImageWidthAndHeight;

    public EfficientNetConfig(ASTConfLangCompilationUnit config) {
        this.saveTrainedArchitecture = (boolean) ASTConfLangCompilationUnitHandler.getValueByKey(config,
                "save_trained_architecture");
        this.architectureSavePath = (String) ASTConfLangCompilationUnitHandler.getValueByKey(config,
                "architecture_save_path");
        this.trainAlgorithmName = (String) ASTConfLangCompilationUnitHandler.getValueByKey(config,
                "train_algorithm_name");
        this.flopsConditionValue = (float) (double) ASTConfLangCompilationUnitHandler.getValueByKey(config,
                "flops_condition_value");

        float minScalingFactorsAlpha = (float) (double) ASTConfLangCompilationUnitHandler.getValueByKey(config,
                "min_scaling_factors_alpha");
        float minScalingFactorsBeta = (float) (double) ASTConfLangCompilationUnitHandler.getValueByKey(config,
                "min_scaling_factors_beta");
        float minScalingFactorsGamma = (float) (double) ASTConfLangCompilationUnitHandler.getValueByKey(config,
                "min_scaling_factors_gamma");
        this.minScalingFactors = new ScalingFactors(minScalingFactorsAlpha, minScalingFactorsBeta,
                minScalingFactorsGamma);

        float maxScalingFactorsAlpha = (float) (double) ASTConfLangCompilationUnitHandler.getValueByKey(config,
                "max_scaling_factors_alpha");
        float maxScalingFactorsBeta = (float) (double) ASTConfLangCompilationUnitHandler.getValueByKey(config,
                "max_scaling_factors_beta");
        float maxScalingFactorsGamma = (float) (double) ASTConfLangCompilationUnitHandler.getValueByKey(config,
                "max_scaling_factors_gamma");
        this.maxScalingFactors = new ScalingFactors(maxScalingFactorsAlpha, maxScalingFactorsBeta,
                maxScalingFactorsGamma);

        float scalingFactorsStepSizeAlpha = (float) (double) ASTConfLangCompilationUnitHandler.getValueByKey(config,
                "scaling_factors_stepsize_alpha");
        float scalingFactorsStepSizeBeta = (float) (double) ASTConfLangCompilationUnitHandler.getValueByKey(config,
                "scaling_factors_stepsize_beta");
        float scalingFactorsStepSizeGamma = (float) (double) ASTConfLangCompilationUnitHandler.getValueByKey(config,
                "scaling_factors_stepsize_gamma");
        this.scalingFactorsStepSize = new ScalingFactors(scalingFactorsStepSizeAlpha, scalingFactorsStepSizeBeta,
                scalingFactorsStepSizeGamma);

        this.phi = (int) ASTConfLangCompilationUnitHandler.getValueByKey(config, "phi");
        this.numClasses = 10;
    }

    public boolean isSaveTrainedArchitecture() {
        return saveTrainedArchitecture;
    }

    public String getArchitectureSavePath() {
        return architectureSavePath;
    }

    public String getTrainAlgorithmName() {
        return trainAlgorithmName;
    }

    public float getFlopsConditionValue() {
        return flopsConditionValue;
    }

    public ScalingFactors getMinScalingFactors() {
        return minScalingFactors;
    }

    public ScalingFactors getMaxScalingFactors() {
        return maxScalingFactors;
    }

    public ScalingFactors getScalingFactorsStepSize() {
        return scalingFactorsStepSize;
    }

    public int getMinImageWidthAndHeight() {
        return minImageWidthAndHeight;
    }

    public int getMaxImageWidthAndHeight() {
        return maxImageWidthAndHeight;
    }

    public int getPhi() {
        return phi;
    }

    public int getNumberClasses() {
        return numClasses;
    }
}
