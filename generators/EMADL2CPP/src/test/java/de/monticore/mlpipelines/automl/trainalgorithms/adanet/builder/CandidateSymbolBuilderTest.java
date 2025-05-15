package de.monticore.mlpipelines.automl.trainalgorithms.adanet.builder;

import de.monticore.lang.monticar.cnnarch._ast.ASTArchitectureBuilder;
import de.monticore.lang.monticar.cnnarch._ast.CNNArchMill;
import de.monticore.lang.monticar.cnnarch._symboltable.ParallelCompositeElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.SerialCompositeElementSymbol;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.custom.models.LayerSymbolCustom;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetCandidate;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetComponent;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;


@RunWith(MockitoJUnitRunner.class)
public class CandidateSymbolBuilderTest extends TestCase {
    private final CandidateSymbolBuilder candidateBuilder = new CandidateSymbolBuilder();

    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testBuildAddsEnoughLayers() {
        AdaNetCandidate candidate = new AdaNetCandidate(new AdaNetComponent(3), new ArrayList<>());
        ParallelCompositeElementSymbol newArch = (ParallelCompositeElementSymbol) candidateBuilder.build(candidate);
        SerialCompositeElementSymbol serial = (SerialCompositeElementSymbol) newArch.getElements().get(0);
        assertEquals(6, serial.getElements().size());
    }

    @Test
    public void testBuildAddsMultipleElementsInParallelLayer() {
        List<AdaNetComponent> previousComponents = new ArrayList<>();
        previousComponents.add(new AdaNetComponent(1));
        AdaNetCandidate candidate = new AdaNetCandidate(new AdaNetComponent(3), previousComponents);
        ParallelCompositeElementSymbol newArch = (ParallelCompositeElementSymbol) candidateBuilder.build(candidate);

        SerialCompositeElementSymbol serial = (SerialCompositeElementSymbol) newArch.getElements().get(0);
        ParallelCompositeElementSymbol firstLayer = (ParallelCompositeElementSymbol) serial.getElements().get(0);
        ParallelCompositeElementSymbol secondLayer = (ParallelCompositeElementSymbol) serial.getElements().get(2);
        assertEquals(2, firstLayer.getElements().size());
        assertEquals(1, secondLayer.getElements().size());

    }

    @Test
    public void testBuildAddsParameterToFullyConnected() {
        List<AdaNetComponent> previousComponents = new ArrayList<>();
        previousComponents.add(new AdaNetComponent(1));
        AdaNetCandidate candidate = new AdaNetCandidate(new AdaNetComponent(3), previousComponents);
        ParallelCompositeElementSymbol newArch = (ParallelCompositeElementSymbol) candidateBuilder.build(candidate);

        SerialCompositeElementSymbol serial = (SerialCompositeElementSymbol) newArch.getElements().get(0);
        ParallelCompositeElementSymbol firstLayer = (ParallelCompositeElementSymbol) serial.getElements().get(0);
        SerialCompositeElementSymbol firstLayerParallelElement = (SerialCompositeElementSymbol) firstLayer.getElements()
                .get(0);
        LayerSymbolCustom layerSymbolCustom = (LayerSymbolCustom) firstLayerParallelElement.getElements().get(0);


        ASTArchitectureBuilder astArchitectureBuilder = CNNArchMill.architectureBuilder();
        assertEquals(1, layerSymbolCustom.getArguments().size());
    }
}