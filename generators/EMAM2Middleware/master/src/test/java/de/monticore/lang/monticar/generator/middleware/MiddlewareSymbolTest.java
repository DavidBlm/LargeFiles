/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAPortInstanceSymbol;
import de.monticore.lang.embeddedmontiarc.tagging.middleware.ros.RosConnectionSymbol;
import de.monticore.lang.embeddedmontiarc.tagging.middleware.ros.RosToEmamTagSchema;
import de.monticore.lang.monticar.generator.middleware.helpers.RosHelper;
import de.monticore.lang.monticar.generator.roscpp.helper.TagHelper;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import org.junit.Test;

import static org.junit.Assert.*;

public class MiddlewareSymbolTest extends AbstractSymtabTest {

    @Test
    public void testMWInferFromTags() {
        TaggingResolver taggingResolver = createSymTabAndTaggingResolver("src/test/resources/");
        RosToEmamTagSchema.registerTagTypes(taggingResolver);

        EMAComponentInstanceSymbol componentInstanceSymbol = taggingResolver.<EMAComponentInstanceSymbol>resolve("tests.infer.inferComp", EMAComponentInstanceSymbol.KIND).orElse(null);
        assertNotNull(componentInstanceSymbol);
        TagHelper.resolveTags(taggingResolver, componentInstanceSymbol);

        EMAComponentInstanceSymbol sub1 = componentInstanceSymbol.getSubComponent("sub1").orElse(null);
        assertNotNull(sub1);

        EMAPortInstanceSymbol rosIn = componentInstanceSymbol.getPortInstance("rosIn").orElse(null);
        EMAPortInstanceSymbol rosOut = componentInstanceSymbol.getPortInstance("rosOut").orElse(null);
        EMAPortInstanceSymbol sub1RosIn = sub1.getPortInstance("rosIn").orElse(null);
        EMAPortInstanceSymbol sub1RosOut = sub1.getPortInstance("rosOut").orElse(null);

        assertNotNull(rosIn);
        assertNotNull(rosOut);
        assertNotNull(sub1RosIn);
        assertNotNull(sub1RosOut);

        RosConnectionSymbol rosInRCS = (RosConnectionSymbol) rosIn.getMiddlewareSymbol().orElse(null);
        RosConnectionSymbol rosOutRCS = (RosConnectionSymbol) rosOut.getMiddlewareSymbol().orElse(null);
        RosConnectionSymbol sub1RosInRCS = (RosConnectionSymbol) sub1RosIn.getMiddlewareSymbol().orElse(null);
        RosConnectionSymbol sub1RosOutRCS = (RosConnectionSymbol) sub1RosOut.getMiddlewareSymbol().orElse(null);

        assertNotEquals(rosInRCS, sub1RosInRCS);
        assertNotEquals(rosOutRCS, sub1RosOutRCS);

        RosHelper.fixRosConnectionSymbols(componentInstanceSymbol, false);

        assertEquals(rosInRCS, sub1RosInRCS);
        assertEquals(rosOutRCS, sub1RosOutRCS);
    }
}
