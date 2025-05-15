package de.monticore.mlpipelines;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import junit.framework.TestCase;
import org.junit.Test;

public class ModelLoaderTest extends TestCase {

    @Test
    public void testLoadModelAsArchitectureSymbol() {
        String modelFolderPath = "src/test/resources/models";
        String modelName = "efficientNetB0";

        ArchitectureSymbol arch = ModelLoader.load(modelFolderPath, modelName);
        assertNotNull(arch);
    }

    public void testLoadEfficientnetB0() {
        ArchitectureSymbol arch = ModelLoader.loadEfficientnetB0();
        assertNotNull(arch);
    }

    public void testLoadAdaNetBase() {
        ArchitectureSymbol arch = ModelLoader.loadAdaNetBase();
        assertNotNull(arch);
    }

    public void testLoadAdaNetStart() {
        ArchitectureSymbol arch = ModelLoader.loadAdaNetStart();
        assertNotNull(arch);
    }
}