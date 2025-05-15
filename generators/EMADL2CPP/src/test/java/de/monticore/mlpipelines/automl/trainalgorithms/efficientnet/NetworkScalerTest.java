package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAPortInstanceSymbol;
import de.monticore.lang.math._ast.ASTNumberExpression;
import de.monticore.lang.math._symboltable.expression.MathNumberExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._ast.ASTArchSimpleArithmeticExpression;
import de.monticore.lang.monticar.cnnarch._ast.ASTArchSimpleExpression;
import de.monticore.lang.monticar.cnnarch._symboltable.*;
import de.monticore.lang.monticar.common2._ast.ASTCommonMatrixType;
import de.monticore.lang.monticar.ts.references.MCASTTypeSymbolReference;
import de.monticore.lang.monticar.types2._ast.ASTDimension;
import de.monticore.mlpipelines.ModelLoader;
import de.monticore.mlpipelines.automl.helper.MathNumberExpressionWrapper;
import de.monticore.numberunit._ast.ASTNumberWithUnit;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.Symbol;
import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;


@RunWith(MockitoJUnitRunner.class)
public class NetworkScalerTest extends TestCase {

    private ArchitectureSymbol architecture;
    private NetworkScaler networkScaler;

    @Before
    public void setUp(){
        String modelFolderPath = "src/test/resources/models";
        String modelName = "efficientNetB0";

        this.architecture = ModelLoader.load(modelFolderPath, modelName);
        this.networkScaler = new NetworkScaler();
    }

    @Test
    public void testConstructor(){
        NetworkScaler scaler = new NetworkScaler();
        assertNotNull(scaler);
    }

    @Test
    public void testScale() {
        ScalingFactors scalingFactors = new ScalingFactors(1.4f, 1.4f, 1.4f);
        ArchitectureSymbol archSymbol = networkScaler.scale(this.architecture, scalingFactors, 1);
        assertNotNull(archSymbol);
    }

    @Test
    public void testReturnsOriginalArchitectureIfNoNetworkInstructions() {
        ArchitectureSymbol archSymbol = networkScaler.scale(
                this.architecture,
                new ScalingFactors(1.4f, 1.4f, 1.4f), 1);
        assertEquals(this.architecture, archSymbol);
    }

    @Test
    public void testScaleSetsArchitecture() {
        ScalingFactors scalingFactors = new ScalingFactors(1.4f, 1.4f, 1.4f);
        networkScaler.scale(this.architecture, scalingFactors, 1);
        assertNotNull(networkScaler.getArchitectureSymbol());
    }

    @Test
    public void testScaleSetsDepthFactorWithPhi1(){
        ScalingFactors scalingFactors = new ScalingFactors(1.4f, 1.4f, 1.4f);
        networkScaler.scale(this.architecture, scalingFactors, 1);
        assertEquals(1.4f, networkScaler.getDepthFactor());
    }

    @Test
    public void testScaleSetsWidthFactorWithPhi1(){
        ScalingFactors scalingFactors = new ScalingFactors(1.4f, 1.4f, 1.4f);
        networkScaler.scale(this.architecture, scalingFactors, 1);
        assertEquals(1.4f, networkScaler.getWidthFactor());
    }

    @Test
    public void testScaleSetsImageResolutionFactorWithPhi1(){
        ScalingFactors scalingFactors = new ScalingFactors(1.4f, 1.4f, 1.4f);
        networkScaler.scale(this.architecture, scalingFactors, 1);
        assertEquals(1.4f, networkScaler.getResolutionFactor());
    }

    @Test
    public void testScaleSetsDepthFactorWithPhi2(){
        ScalingFactors scalingFactors = new ScalingFactors(1.4f, 1.4f, 1.4f);
        networkScaler.scale(this.architecture, scalingFactors, 2);
        assertEquals(1.96f, networkScaler.getDepthFactor(), 0.001);
    }

    @Test
    public void testScaleSetsWidthFactorWithPhi2(){
        ScalingFactors scalingFactors = new ScalingFactors(1.4f, 1.4f, 1.4f);
        networkScaler.scale(this.architecture, scalingFactors, 2);
        assertEquals(1.96f, networkScaler.getWidthFactor(), 0.001);
    }

    @Test
    public void testScaleSetsImageResolutionFactorWithPhi2(){
        ScalingFactors scalingFactors = new ScalingFactors(1.4f, 1.4f, 1.4f);
        networkScaler.scale(this.architecture, scalingFactors, 2);
        assertEquals(1.96f, networkScaler.getResolutionFactor(), 0.001);
    }

    @Test
    public void testScaleDepth() {
        ScalingFactors scalingFactors = new ScalingFactors(2, 1, 1);
        int phi = 1;

        ArchitectureSymbol scaledArch = networkScaler.scale(architecture, scalingFactors, phi);

        List<ArchitectureElementSymbol> architectureElements = getElementsFromArchitecture(scaledArch);
        ArchitectureElementSymbol residualBlock1 = architectureElements.get(2);
        ArchitectureElementSymbol residualBlock2 = architectureElements.get(4);
        double residualBlock1Depth = getValueFromElementSymbol(residualBlock1, 3);
        double residualBlock2Depth = getValueFromElementSymbol(residualBlock2, 3);
        double expectedDepth = 8;

        assertEquals(expectedDepth, residualBlock1Depth, 0.0001);
        assertEquals(expectedDepth, residualBlock2Depth, 0.0001);
        assertEquals(expectedDepth, getValueFromElementAst(residualBlock1, 3), 0.0001);
        assertEquals(expectedDepth, getValueFromElementAst(residualBlock2, 3), 0.0001);
    }

    private double getValueFromElementSymbol(ArchitectureElementSymbol element, int numberExpressionIndex) {
        ArchitectureElementScope spannedScope = element.getSpannedScope();
        ArrayList expressions = (ArrayList) spannedScope.getLocalSymbols().get("");
        MathNumberExpressionSymbol expression = (MathNumberExpressionSymbol) expressions.get(numberExpressionIndex);
        double dividend = expression.getValue().getRealNumber().getDividend().longValue();
        double divisor = expression.getValue().getRealNumber().getDivisor().longValue();
        return dividend / divisor;
    }

    private double getValueFromElementAst(ArchitectureElementSymbol element, int numberExpressionIndex) {
        ArchitectureElementScope spannedScope = element.getSpannedScope();
        ArrayList expressions = (ArrayList) spannedScope.getLocalSymbols().get("");
        MathNumberExpressionSymbol expression = (MathNumberExpressionSymbol) expressions.get(numberExpressionIndex);
        ASTArchSimpleExpression astExpression = (ASTArchSimpleExpression) expression.getAstNode().get();
        return getValueOfExpression(astExpression);
    }

    private static double getValueOfExpression(ASTArchSimpleExpression expression) {
        ASTArchSimpleArithmeticExpression arithmeticExpression = (ASTArchSimpleArithmeticExpression) expression.getArithmeticExpression();
        ASTNumberExpression numberExpression = arithmeticExpression.getNumberExpression();
        ASTNumberWithUnit numberWithUnit = numberExpression.getNumberWithUnit();
        return numberWithUnit.getNumber().get();
    }

    @Test
    public void testScaleDepthRound() {
        ScalingFactors scalingFactors = new ScalingFactors(1.2, 1, 1);
        int phi = 1;

        ArchitectureSymbol scaledArch = networkScaler.scale(architecture, scalingFactors, phi);

        List<ArchitectureElementSymbol> architectureElements = getElementsFromArchitecture(scaledArch);
        ArchitectureElementSymbol residualBlock1 = architectureElements.get(2);
        ArchitectureElementSymbol residualBlock2 = architectureElements.get(4);
        double residualBlock1Depth = getValueFromElementSymbol(residualBlock1, 3);
        double residualBlock2Depth = getValueFromElementSymbol(residualBlock2, 3);
        double expectedDepth = 5;

        assertEquals(expectedDepth, residualBlock1Depth, 0.0001);
        assertEquals(expectedDepth, residualBlock2Depth, 0.0001);
        assertEquals(expectedDepth, getValueFromElementAst(residualBlock1, 3), 0.0001);
        assertEquals(expectedDepth, getValueFromElementAst(residualBlock2, 3), 0.0001);
    }

    @Test
    public void testScaleWidthResidualBlocks() {
        ScalingFactors scalingFactors = new ScalingFactors(1, 2, 1);
        int phi = 1;

        ArchitectureSymbol scaledArch = networkScaler.scale(architecture, scalingFactors, phi);

        List<ArchitectureElementSymbol> architectureElements = getElementsFromArchitecture(scaledArch);
        ArchitectureElementSymbol residualBlock1 = architectureElements.get(2);
        ArchitectureElementSymbol residualBlock2 = architectureElements.get(4);
        double residualBlock1Width = getValueFromElementSymbol(residualBlock1, 5);
        double residualBlock2Width = getValueFromElementSymbol(residualBlock2, 5);

        double expectedResidualWidth1 = 96;
        double expectedResidualWidth2 = 192;

        assertEquals(expectedResidualWidth1, residualBlock1Width, 0.0001);
        assertEquals(expectedResidualWidth2, residualBlock2Width, 0.0001);
        assertEquals(expectedResidualWidth1, getValueFromElementAst(residualBlock1, 5), 0.0001);
        assertEquals(expectedResidualWidth2, getValueFromElementAst(residualBlock2, 5), 0.0001);
    }

    @Test
    public void testScaleImageResolution(){
        ScalingFactors scalingFactors = new ScalingFactors(1, 1, 2);
        int phi = 1;

        ArchitectureSymbol scaledArch = networkScaler.scale(architecture, scalingFactors, phi);
        ASTDimension dimensions = getAstDimension(scaledArch);
        MathNumberExpressionSymbol heightDim = (MathNumberExpressionSymbol)dimensions.getMatrixDim(1).getSymbol();
        MathNumberExpressionSymbol widthDim = (MathNumberExpressionSymbol) dimensions.getMatrixDim(2).getSymbol();

        MathNumberExpressionWrapper imageHeight = new MathNumberExpressionWrapper(heightDim);
        MathNumberExpressionWrapper imageWidth = new MathNumberExpressionWrapper(widthDim);

        float expectedHeight = 32;
        float expectedWidth = 32;
        assertEquals(expectedHeight, imageHeight.getFloatValue(), 0.0001);
        assertEquals(expectedWidth, imageWidth.getFloatValue(), 0.0001);
    }

    @Test
    public void testScaleImageResolutionRound() {
        ScalingFactors scalingFactors = new ScalingFactors(1, 1, 1.1);
        int phi = 1;

        ArchitectureSymbol scaledArch = networkScaler.scale(architecture, scalingFactors, phi);
        ASTDimension dimensions = getAstDimension(scaledArch);
        MathNumberExpressionSymbol heightDim = (MathNumberExpressionSymbol) dimensions.getMatrixDim(1).getSymbol();
        MathNumberExpressionSymbol widthDim = (MathNumberExpressionSymbol) dimensions.getMatrixDim(2).getSymbol();

        MathNumberExpressionWrapper imageHeight = new MathNumberExpressionWrapper(heightDim);
        MathNumberExpressionWrapper imageWidth = new MathNumberExpressionWrapper(widthDim);

        float expectedHeight = 18;
        float expectedWidth = 18;
        assertEquals(expectedHeight, imageHeight.getFloatValue(), 0.0001);
        assertEquals(expectedWidth, imageWidth.getFloatValue(), 0.0001);
    }

    private static ASTDimension getAstDimension(ArchitectureSymbol scaledArch) {
        Scope spannedScope = scaledArch.getSpannedScope();
        Scope enclosingScope = spannedScope.getEnclosingScope().get();

        Map<String, Collection<Symbol>> enclosedSymbols = enclosingScope.getLocalSymbols();
        ArrayList image = (ArrayList) enclosedSymbols.get("image");
        EMAPortInstanceSymbol instanceSymbols = (EMAPortInstanceSymbol) image.get(0);
        MCASTTypeSymbolReference typeReference = (MCASTTypeSymbolReference) instanceSymbols.getTypeReference();
        ASTCommonMatrixType astType = (ASTCommonMatrixType) typeReference.getAstType();
        ASTDimension dimensions = astType.getDimension();
        return dimensions;
    }

    private List<ArchitectureElementSymbol> getElementsFromArchitecture(ArchitectureSymbol arch) {
        NetworkInstructionSymbol networkInstruction = arch.getNetworkInstructions().get(0);
        SerialCompositeElementSymbol networkInstructionBody = networkInstruction.getBody();
        List<ArchitectureElementSymbol> architectureElements = networkInstructionBody.getElements();
        return architectureElements;
    }

    @Test
    public void testScaleWidthStem() {
        ScalingFactors scalingFactors = new ScalingFactors(1, 2, 1);
        int phi = 1;

        ArchitectureSymbol scaledArch = networkScaler.scale(architecture, scalingFactors, phi);

        List<ArchitectureElementSymbol> architectureElements = getElementsFromArchitecture(scaledArch);
        ArchitectureElementSymbol stem = architectureElements.get(1);
        double stemWidth = getValueFromElementSymbol(stem, 1);

        double expectedStemWidth = 16;

        assertEquals(expectedStemWidth, stemWidth, 0.0001);
        assertEquals(expectedStemWidth, getValueFromElementAst(stem, 1), 0.0001);
    }

    @Test
    public void testScaleWidthResidualBlocksRound() {
        ScalingFactors scalingFactors = new ScalingFactors(1, 1.1, 1);
        int phi = 1;

        ArchitectureSymbol scaledArch = networkScaler.scale(architecture, scalingFactors, phi);

        List<ArchitectureElementSymbol> architectureElements = getElementsFromArchitecture(scaledArch);
        ArchitectureElementSymbol residualBlock1 = architectureElements.get(2);
        ArchitectureElementSymbol residualBlock2 = architectureElements.get(4);
        double residualBlock1Width = getValueFromElementSymbol(residualBlock1, 5);
        double residualBlock2Width = getValueFromElementSymbol(residualBlock2, 5);

        double expectedResidualWidth1 = 53;
        double expectedResidualWidth2 = 106;

        assertEquals(expectedResidualWidth1, residualBlock1Width, 0.0001);
        assertEquals(expectedResidualWidth2, residualBlock2Width, 0.0001);
        assertEquals(expectedResidualWidth1, getValueFromElementAst(residualBlock1, 5), 0.0001);
        assertEquals(expectedResidualWidth2, getValueFromElementAst(residualBlock2, 5), 0.0001);
    }

    @Test
    public void testScaleWidthReductionBlocks() {
        ScalingFactors scalingFactors = new ScalingFactors(1, 2, 1);
        int phi = 1;

        ArchitectureSymbol scaledArch = networkScaler.scale(architecture, scalingFactors, phi);

        List<ArchitectureElementSymbol> architectureElements = getElementsFromArchitecture(scaledArch);
        ArchitectureElementSymbol reductionBlock1 = architectureElements.get(3);
        ArchitectureElementSymbol reductionBlock2 = architectureElements.get(5);
        double reductionBlock1Width = getValueFromElementSymbol(reductionBlock1, 1);
        double reductionBlock2Width = getValueFromElementSymbol(reductionBlock2, 1);
        double expectedReductionWidth1 = 96;
        double expectedReductionWidth2 = 192;

        assertEquals(expectedReductionWidth1, reductionBlock1Width, 0.0001);
        assertEquals(expectedReductionWidth2, reductionBlock2Width, 0.0001);
        assertEquals(expectedReductionWidth1, getValueFromElementAst(reductionBlock1, 1), 0.0001);
        assertEquals(expectedReductionWidth2, getValueFromElementAst(reductionBlock2, 1), 0.0001);
    }
}