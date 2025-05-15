package de.monticore.mlpipelines.automl.helper;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAPortInstanceSymbol;
import de.monticore.lang.math._ast.ASTNumberExpression;
import de.monticore.lang.math._symboltable.expression.MathNumberExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureElementScope;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.LayerSymbol;
import de.monticore.lang.monticar.common2._ast.ASTCommonMatrixType;
import de.monticore.lang.monticar.ts.references.MCASTTypeSymbolReference;
import de.monticore.lang.monticar.types2._ast.ASTDimension;
import de.monticore.mlpipelines.automl.trainalgorithms.ASTGenerator;
import de.monticore.symboltable.Symbol;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ArchitectureHelper {

    public static ASTDimension getImageDimension(ArchitectureSymbol architecture) {
        Map<String, Collection<Symbol>> enclosedSymbols = architecture.getSpannedScope()
                .getEnclosingScope()
                .get()
                .getLocalSymbols();
        ArrayList image = (ArrayList) enclosedSymbols.get("image");
        EMAPortInstanceSymbol instanceSymbols = (EMAPortInstanceSymbol) image.get(0);
        MCASTTypeSymbolReference typeReference = (MCASTTypeSymbolReference) instanceSymbols.getTypeReference();
        ASTCommonMatrixType astType = (ASTCommonMatrixType) typeReference.getAstType();
        ASTDimension dimensions = astType.getDimension();
        return dimensions;
    }

    public static List<LayerSymbol> getLayerSymbols(ArchitectureSymbol architecture) {
        List<ArchitectureElementSymbol> layers = architecture.getNetworkInstructions().get(0).getBody().getElements();
        List<LayerSymbol> layerSymbols = new ArrayList<>();
        for (ArchitectureElementSymbol layer : layers) {
            if (layer instanceof LayerSymbol) {
                layerSymbols.add((LayerSymbol) layer);
            }
        }
        return layerSymbols;
    }

    public static ArrayList getExpressions(ArchitectureElementSymbol architectureElement) {
        ArchitectureElementScope spannedScope = architectureElement.getSpannedScope();
        ArrayList expressions = (ArrayList) spannedScope.getLocalSymbols()
                .get(""); //the MathNumberExpressionSymbol is in the key ""
        return expressions;
    }

    public static void setImageASTMatrixDimensions(ASTDimension dimensions, ASTNumberExpression matrixDim1, ASTNumberExpression matrixDim2) {
        ASTNumberExpression numberExpression1 = ASTGenerator.createNumberExpression(
                (MathNumberExpressionSymbol) matrixDim1.getSymbol());
        ASTNumberExpression numberExpression2 = ASTGenerator.createNumberExpression(
                (MathNumberExpressionSymbol) matrixDim2.getSymbol());

        dimensions.setMatrixDim(1, numberExpression1);
        dimensions.setMatrixDim(1, numberExpression2);
        dimensions.setMatrixDim(2, numberExpression1);
        dimensions.setMatrixDim(2, numberExpression2);
    }

    public static int getOriginalImageDimension(ArchitectureSymbol startNetwork) {
        ASTDimension dimensions = ArchitectureHelper.getImageDimension(startNetwork);
        ASTNumberExpression matrixDim1 = (ASTNumberExpression) dimensions.getMatrixDim(1);
        MathNumberExpressionSymbol heightDim = (MathNumberExpressionSymbol) matrixDim1.getSymbol();
        MathNumberExpressionWrapper imageHeight = new MathNumberExpressionWrapper(heightDim);
        return imageHeight.getIntValue();
    }

    public static int getMathExpressionValueAt(ArchitectureElementSymbol architectureElement, int index) {
        ArrayList symbolExpressions = ArchitectureHelper.getExpressions(architectureElement);
        MathNumberExpressionSymbol mathNumberExpression = (MathNumberExpressionSymbol) symbolExpressions.get(
                index);
        MathNumberExpressionWrapper expression = new MathNumberExpressionWrapper(mathNumberExpression);
        return expression.getIntValue();
    }
}
