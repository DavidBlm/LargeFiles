package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.expressionsbasis._ast.ASTExpression;
import de.monticore.lang.math._ast.ASTNumberExpression;
import de.monticore.lang.math._symboltable.expression.MathNumberExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.NetworkInstructionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.SerialCompositeElementSymbol;
import de.monticore.lang.monticar.types2._ast.ASTDimension;
import de.monticore.mlpipelines.automl.helper.ArchitectureHelper;
import de.monticore.mlpipelines.automl.helper.MathNumberExpressionWrapper;
import de.monticore.mlpipelines.automl.helper.OriginalLayerParams;
import de.monticore.mlpipelines.automl.trainalgorithms.ASTGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NetworkScaler {
    List<OriginalLayerParams> parametersReference;

    private float depthFactor = 0;
    private float widthFactor = 0;
    private float resolutionFactor = 0;
    private ArchitectureSymbol architecture = null;

    public ArchitectureSymbol scale(ArchitectureSymbol originalArchitecture, ScalingFactors scalingFactors, int phi) {
        this.architecture = originalArchitecture;
        calculateFactors(scalingFactors, phi);

        if (this.architecture.getNetworkInstructions().isEmpty())
            return originalArchitecture;

        scaleDimensions();
        return getArchitectureSymbol();
    }

    private void calculateFactors(ScalingFactors scalingFactors, int phi) {
        this.depthFactor = (float) Math.pow(scalingFactors.alpha, phi);
        this.widthFactor = (float) Math.pow(scalingFactors.beta, phi);
        this.resolutionFactor = (float) Math.pow(scalingFactors.gamma, phi);
    }

    private void scaleDimensions() {
        scaleDepth();
        scaleWidth();
        scaleImageResolution();
    }

    private static int getChannelsIndex(String architectureElementName) {
        switch (architectureElementName) {
            case "residualBlock":
                return 5;
            case "reductionBlock":
            case "stem":
                return 1;
        }
        throw new IllegalArgumentException("Block type not supported");
    }

    private void scaleDepth() {
        List<ArchitectureElementSymbol> architectureElements = findArchitectureElements();

        for (ArchitectureElementSymbol architectureElement : architectureElements) {
            if (!architectureElement.getName().equals("residualBlock"))
                continue;
            scaleNetworkElementDepth(architectureElement);
        }
    }

    private void scaleWidth() {
        List<ArchitectureElementSymbol> architectureElements = findArchitectureElements();

        for (ArchitectureElementSymbol architectureElement : architectureElements) {
            List<String> allowedLayers = Arrays.asList("residualBlock", "reductionBlock", "stem");
            if (!allowedLayers.contains(architectureElement.getName()))
                continue;

            scaleArchitectureElementWidth(architectureElement);
        }
    }

    public ArchitectureSymbol getArchitectureSymbol() {
        return architecture;
    }

    private void scaleImageResolution() {
        ASTDimension dimensions = ArchitectureHelper.getImageDimension(architecture);
        changeImageDimension(dimensions);
    }

    private void changeImageDimension(ASTDimension dimensions) {
        ASTNumberExpression matrixDim1 = (ASTNumberExpression) dimensions.getMatrixDim(1);
        ASTNumberExpression matrixDim2 = (ASTNumberExpression) dimensions.getMatrixDim(2);

        setImageDimensionSymbolValue(matrixDim1, matrixDim2);
        ArchitectureHelper.setImageASTMatrixDimensions(dimensions, matrixDim1, matrixDim2);
    }

    private void scaleArchitectureElementWidth(ArchitectureElementSymbol architectureElement) {
        ArrayList expressions = ArchitectureHelper.getExpressions(architectureElement);
        int channelsIndex = getChannelsIndex(architectureElement.getName());
        MathNumberExpressionSymbol mathNumberExpression = (MathNumberExpressionSymbol) expressions.get(channelsIndex);
        MathNumberExpressionWrapper expression = new MathNumberExpressionWrapper(mathNumberExpression);
        expression.scaleRound(this.widthFactor);
        ASTGenerator.createMathNumberExpression(mathNumberExpression);
    }

    private void setImageDimensionSymbolValue(ASTExpression matrixDim1, ASTExpression matrixDim2) {
        MathNumberExpressionSymbol heightDim = (MathNumberExpressionSymbol) matrixDim1.getSymbol();
        MathNumberExpressionSymbol widthDim = (MathNumberExpressionSymbol) matrixDim2.getSymbol();
        MathNumberExpressionWrapper imageHeight = new MathNumberExpressionWrapper(heightDim);
        MathNumberExpressionWrapper imageWidth = new MathNumberExpressionWrapper(widthDim);

        int newDimension = roundMathNumberExpressionToInt(imageHeight);
        imageHeight.setValue(newDimension);
        imageWidth.setValue(newDimension);
    }

    private List<ArchitectureElementSymbol> findArchitectureElements() {
        NetworkInstructionSymbol networkInstruction = this.architecture.getNetworkInstructions().get(0);
        SerialCompositeElementSymbol networkInstructionBody = networkInstruction.getBody();
        List<ArchitectureElementSymbol> architectureElements = networkInstructionBody.getElements();
        return architectureElements;
    }

    private void scaleNetworkElementDepth(ArchitectureElementSymbol architectureElement) {
        ArrayList symbolExpressions = ArchitectureHelper.getExpressions(architectureElement);
        int depthIndex = 3;
        MathNumberExpressionSymbol mathNumberExpression = (MathNumberExpressionSymbol) symbolExpressions.get(
                depthIndex);
        MathNumberExpressionWrapper expression = new MathNumberExpressionWrapper(mathNumberExpression);
        expression.scaleRound(this.depthFactor);
        ASTGenerator.createMathNumberExpression(mathNumberExpression);
    }

    private int roundMathNumberExpressionToInt(MathNumberExpressionWrapper imageDim) {
        float oldDimension = imageDim.getFloatValue();
        int newDimension = Math.round(this.resolutionFactor * oldDimension);
        return newDimension;
    }

    private void rollbackElementLevelChanges(){
        NetworkInstructionSymbol networkInstruction = this.architecture.getNetworkInstructions().get(0);
        SerialCompositeElementSymbol networkInstructionBody = networkInstruction.getBody();
        List<ArchitectureElementSymbol> architectureElements = networkInstructionBody.getElements();
        int i=0;
        List<String> allowedLayers = Arrays.asList("residualBlock", "reductionBlock", "stem");
        for (ArchitectureElementSymbol architectureElement : architectureElements) {
            int channelsIndex=1;
            if (allowedLayers.contains(architectureElement.getName())){

                if (architectureElement.getName().equals("residualBlock")){
                    channelsIndex = 5;
                    int depthIndex = 3;
                    rollbackScaling(architectureElement, depthIndex, parametersReference.get(i).getOriginalDepthValue());
                }
                rollbackScaling(architectureElement, channelsIndex, parametersReference.get(i).getOriginalChannelValue());

            }
            i += 1;
        }
    }

    private void rollbackScaling(ArchitectureElementSymbol architectureElement, int depthIndex, int parametersReference) {
        ArrayList symbolExpressions = ArchitectureHelper.getExpressions(architectureElement);

        MathNumberExpressionSymbol mathNumberExpression = (MathNumberExpressionSymbol) symbolExpressions.get(
                depthIndex);
        MathNumberExpressionWrapper expression = new MathNumberExpressionWrapper(mathNumberExpression);
        expression.setValue(parametersReference);
    }

    private void rollbackImageScaling(){
        ASTDimension dimensions = ArchitectureHelper.getImageDimension(architecture);
        ASTNumberExpression matrixDim1 = (ASTNumberExpression) dimensions.getMatrixDim(1);
        ASTNumberExpression matrixDim2 = (ASTNumberExpression) dimensions.getMatrixDim(2);
        MathNumberExpressionSymbol heightDim = (MathNumberExpressionSymbol) matrixDim1.getSymbol();
        MathNumberExpressionSymbol widthDim = (MathNumberExpressionSymbol) matrixDim2.getSymbol();
        MathNumberExpressionWrapper imageHeight = new MathNumberExpressionWrapper(heightDim);
        MathNumberExpressionWrapper imageWidth = new MathNumberExpressionWrapper(widthDim);

        imageHeight.setValue(OriginalLayerParams.getImageDimensionValue());
        imageWidth.setValue(OriginalLayerParams.getImageDimensionValue());
        ArchitectureHelper.setImageASTMatrixDimensions(dimensions, matrixDim1, matrixDim2);
    }

    protected void rollbackScaledNetwork(){
        rollbackElementLevelChanges(); //depth and channels
        rollbackImageScaling(); //image dimensions
    }

    public float getDepthFactor() {
        return depthFactor;
    }

    public float getWidthFactor() {
        return widthFactor;
    }

    public float getResolutionFactor() {
        return resolutionFactor;
    }

}
