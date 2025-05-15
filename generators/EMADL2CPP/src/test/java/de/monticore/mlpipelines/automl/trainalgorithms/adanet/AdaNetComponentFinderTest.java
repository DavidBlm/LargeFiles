package de.monticore.mlpipelines.automl.trainalgorithms.adanet;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetComponent;
import junit.framework.TestCase;

import java.util.List;

public class AdaNetComponentFinderTest extends TestCase {

    public void testConstructor() {
        AdaNetComponentFinder componentFinder = new AdaNetComponentFinder();
        assertNotNull(componentFinder);
    }

    public void testFindComponents() {
        ArchitectureSymbol startNetwork = new ArchitectureSymbol();
        int minDepth = 1;
        AdaNetComponentFinder componentFinder = new AdaNetComponentFinder();
        List<AdaNetComponent> components = componentFinder.findComponents(minDepth);

        int firstComponentDepth = components.get(0).getDepth();
        int secondComponentDepth = components.get(1).getDepth();
        int expectedFirstComponentDepth = 1;
        int expectedSecondComponentDepth = 2;

        assertEquals(expectedFirstComponentDepth, firstComponentDepth);
        assertEquals(expectedSecondComponentDepth, secondComponentDepth);
    }
}