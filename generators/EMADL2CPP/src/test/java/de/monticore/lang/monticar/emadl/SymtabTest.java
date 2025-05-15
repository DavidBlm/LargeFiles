/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAComponentSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.emadl._parser.EMADLParser;
import de.monticore.lang.monticar.emadl.generator.Backend;
import de.monticore.symboltable.Scope;
import de.se_rwth.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class SymtabTest extends AbstractSymtabTest {

    @Before
    public void setUp() {
        // ensure an empty log
        Log.getFindings().clear();
        Log.enableFailQuick(false);
    }

    @Test
    public void testParsing() throws Exception {
        EMADLParser parser = new EMADLParser();
        assertTrue(parser.parse("src/test/resources/models/Alexnet.emadl").isPresent());
    }

    @Test
    public void testAlexnet(){
        Scope symTab = createSymTab("src/test/resources/models");
        EMAComponentSymbol a = symTab.<EMAComponentSymbol>resolve("ResNet34", EMAComponentSymbol.KIND).orElse(null);
        EMAComponentInstanceSymbol c = symTab.<EMAComponentInstanceSymbol>resolve("resNet34", EMAComponentInstanceSymbol.KIND).orElse(null);
        assertNotNull(a);
    }

    @Test
    public void testCustomMNISTCalculator(){
        Scope symTab = createSymTab("src/test/resources/custom_files", "", Backend.GLUON, "./target/ComposedNetworks", "src/test/resources/models/customMNISTCalculator");
        EMAComponentSymbol a = symTab.<EMAComponentSymbol>resolve("cNNCalculator.Connector", EMAComponentSymbol.KIND).orElse(null);
        assertNotNull(a);
    }

}
