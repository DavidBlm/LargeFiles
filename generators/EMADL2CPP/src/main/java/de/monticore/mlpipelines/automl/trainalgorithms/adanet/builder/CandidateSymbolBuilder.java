package de.monticore.mlpipelines.automl.trainalgorithms.adanet.builder;

import de.monticore.lang.math._symboltable.JSValue;
import de.monticore.lang.math._symboltable.expression.MathNumberExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArgumentSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.LayerSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ParallelCompositeElementSymbol;
import de.monticore.mlpipelines.automl.helper.RationalMath;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.custom.models.*;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetCandidate;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.ParallelCandidateLayer;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.ParallelCandidateLayerElement;

import java.util.ArrayList;
import java.util.List;

public class CandidateSymbolBuilder {
    AdaNetCandidate candidate;

    public ParallelCompositeElementSymbol build(AdaNetCandidate candidate) {
        this.candidate = candidate;
        return getCandidateSymbol();
    }

    private ParallelCompositeElementSymbol getCandidateSymbol() {
        ParallelCompositeElementSymbolCustom candidateSymbol = new ParallelCompositeElementSymbolCustom();
        List<ArchitectureElementSymbol> parallelElements = getRootSerialCompositeElement();
        candidateSymbol.setElements(parallelElements);

        return candidateSymbol;
    }

    private List<ArchitectureElementSymbol> getRootSerialCompositeElement() {
        SerialCompositeElementSymbolCustom compositeElementSymbol = new SerialCompositeElementSymbolCustom();

        List<ArchitectureElementSymbol> parallelElements = new ArrayList<>();
        parallelElements.add(compositeElementSymbol);

        List<ArchitectureElementSymbol> serialLayers = getLayers();
        compositeElementSymbol.setElements(serialLayers);

        return parallelElements;
    }

    private List<ArchitectureElementSymbol> getLayers() {
        List<ArchitectureElementSymbol> serialElements = new ArrayList<>();
        for (ParallelCandidateLayer layer : this.candidate.getParallelCandidateLayers()) {
            ParallelCompositeElementSymbol parallelLayer = getParallelLayer(layer);
            serialElements.add(parallelLayer);
            serialElements.add(getConcatenateLayer());
            serialElements.add(getReluLayer());
        }
        return serialElements;
    }

    private ParallelCompositeElementSymbol getParallelLayer(ParallelCandidateLayer layer) {
        ParallelCompositeElementSymbolCustom parallelSymbol = new ParallelCompositeElementSymbolCustom();
        List<ArchitectureElementSymbol> elements = new ArrayList<>();
        for (ParallelCandidateLayerElement element : layer.getElements()) {
            SerialCompositeElementSymbolCustom parallelElement = getParallelLayerElement(element);
            elements.add(parallelElement);
        }
        parallelSymbol.setElements(elements);
        return parallelSymbol;
    }

    private LayerSymbol getConcatenateLayer() {
        LayerSymbolCustom layer = new LayerSymbolCustom("Concatenate");
        return layer;
    }

    private LayerSymbol getReluLayer() {
        LayerSymbolCustom layer = new LayerSymbolCustom("Relu");
        return layer;
    }

    private SerialCompositeElementSymbolCustom getParallelLayerElement(ParallelCandidateLayerElement element) {
        SerialCompositeElementSymbolCustom serialSymbol = new SerialCompositeElementSymbolCustom();
        LayerSymbol layer = getFullyConnectedLayer(element.getUnits());
        List<ArchitectureElementSymbol> elements = new ArrayList<>();
        elements.add(layer);
        serialSymbol.setElements(elements);
        return serialSymbol;
    }

    private LayerSymbol getFullyConnectedLayer(int units) {
        LayerSymbolCustom layer = new LayerSymbolCustom("FullyConnected");
        List<ArgumentSymbol> arguments = new ArrayList<>();
        arguments.add(getUnitsArgumentSymbol(units));
        layer.setArguments(arguments);
        return layer;
    }

    private static ArgumentSymbol getUnitsArgumentSymbol(int units) {
        ArgumentSymbolCustom unitsArgument = new ArgumentSymbolCustom("units");
        MathNumberExpressionSymbol unitsValue = getMathNumberExpressionSymbol(units);
        ArchSimpleExpressionSymbolCustom archSimpleExpressionSymbol = new ArchSimpleExpressionSymbolCustom();
        archSimpleExpressionSymbol.setMathExpression(unitsValue);
        unitsArgument.setRhs(archSimpleExpressionSymbol);
        return unitsArgument;
    }

    private static MathNumberExpressionSymbol getMathNumberExpressionSymbol(int value) {
        MathNumberExpressionSymbol unitsValue = new MathNumberExpressionSymbol();
        JSValue jsValue = new JSValue(RationalMath.of(value));
        unitsValue.setValue(jsValue);
        return unitsValue;
    }
}
