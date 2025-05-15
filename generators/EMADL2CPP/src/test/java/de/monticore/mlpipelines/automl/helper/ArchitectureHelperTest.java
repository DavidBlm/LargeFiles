package de.monticore.mlpipelines.automl.helper;

import de.monticore.lang.math._symboltable.expression.MathNumberExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.LayerSymbol;
import de.monticore.lang.monticar.types2._ast.ASTDimension;
import de.monticore.mlpipelines.ModelLoader;
import junit.framework.TestCase;

import java.util.List;

public class ArchitectureHelperTest extends TestCase {

    public void testGetImageDimension() {
        ArchitectureSymbol architectureSymbol = ModelLoader.loadEfficientnetB0();
        ASTDimension imageDimension = ArchitectureHelper.getImageDimension(architectureSymbol);
        MathNumberExpressionSymbol heightDim = (MathNumberExpressionSymbol) imageDimension.getMatrixDim(1).getSymbol();
        MathNumberExpressionSymbol widthDim = (MathNumberExpressionSymbol) imageDimension.getMatrixDim(2).getSymbol();

        MathNumberExpressionWrapper imageHeight = new MathNumberExpressionWrapper(heightDim);
        MathNumberExpressionWrapper imageWidth = new MathNumberExpressionWrapper(widthDim);

        assertEquals(16, imageHeight.getIntValue());
        assertEquals(16, imageWidth.getIntValue());
    }

    public void testGetLayers() {
        ArchitectureSymbol architectureSymbol = ModelLoader.loadEfficientnetB0();
        List<LayerSymbol> layers = ArchitectureHelper.getLayerSymbols(architectureSymbol);
        assertEquals(6, layers.size());
        assertEquals("stem", layers.get(0).getName());
    }

    public void testGetArchitectureElements() {
        ArchitectureSymbol architectureSymbol = ModelLoader.loadEfficientnetB0();
        ArchitectureElementSymbol layer = ArchitectureHelper.getLayerSymbols(architectureSymbol).get(1);
        List<ArchitectureElementSymbol> architectureElements = ArchitectureHelper.getExpressions(layer);
        assertEquals(6, architectureElements.size());
    }
}