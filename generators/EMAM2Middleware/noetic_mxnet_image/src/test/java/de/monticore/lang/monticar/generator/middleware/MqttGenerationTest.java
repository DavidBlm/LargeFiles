/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.embeddedmontiarc.tagging.middleware.mqtt.MqttToEmamTagSchema;
import de.monticore.lang.monticar.generator.middleware.impls.CPPGenImpl;
import de.monticore.lang.monticar.generator.middleware.impls.MqttGenImpl;
import de.monticore.lang.monticar.generator.mqtt.helper.MqttTagHelper;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class MqttGenerationTest extends AbstractSymtabTest {
    private static final String TEST_PATH = "src/test/resources/";
    private static final String OUT_BASE = "./target/generated-sources-mqtt/";

    @Test
    public void testMqttGeneration() throws IOException {
        TaggingResolver taggingResolver = createSymTabAndTaggingResolver(TEST_PATH);
        MqttToEmamTagSchema.registerTagTypes(taggingResolver);

        EMAComponentInstanceSymbol componentInstanceSymbol = taggingResolver.<EMAComponentInstanceSymbol>resolve("tests.mqtt.testComp", EMAComponentInstanceSymbol.KIND).orElse(null);
        assertNotNull(componentInstanceSymbol);
        
        MqttTagHelper.resolveTags(taggingResolver, componentInstanceSymbol);

        DistributedTargetGenerator distributedTargetGenerator = new DistributedTargetGenerator();
        distributedTargetGenerator.setGenerationTargetPath(OUT_BASE + "compA/src");
        distributedTargetGenerator.add(new CPPGenImpl(TEST_PATH),"cpp"); // Cpp implementation for the component
        distributedTargetGenerator.add(new MqttGenImpl(), "mqtt");
        List<File> files = distributedTargetGenerator.generate(componentInstanceSymbol, taggingResolver);

    }
}
