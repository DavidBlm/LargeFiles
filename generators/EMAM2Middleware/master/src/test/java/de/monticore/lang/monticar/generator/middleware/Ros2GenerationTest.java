/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.embeddedmontiarc.tagging.middleware.ros.RosToEmamTagSchema;
import de.monticore.lang.monticar.generator.middleware.impls.CPPGenImpl;
import de.monticore.lang.monticar.generator.middleware.impls.RclCppGenImpl;
import de.monticore.lang.monticar.generator.roscpp.helper.TagHelper;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertNotNull;

public class Ros2GenerationTest extends AbstractSymtabTest {
    private static final String TEST_PATH = "src/test/resources/";
    private static final String OUT_BASE = "./target/generated-sources-ros2/";

    @Test
    public void testRos2Generation() throws IOException {
        TaggingResolver taggingResolver = createSymTabAndTaggingResolver(TEST_PATH);
        EMAComponentInstanceSymbol componentInstanceSymbol = taggingResolver.<EMAComponentInstanceSymbol>resolve("tests.aRos2.addComp", EMAComponentInstanceSymbol.KIND).orElse(null);
        RosToEmamTagSchema.registerTagTypes(taggingResolver);

        assertNotNull(componentInstanceSymbol);
        TagHelper.resolveTags(taggingResolver, componentInstanceSymbol);

        DistributedTargetGenerator distributedTargetGenerator = new DistributedTargetGenerator();
        distributedTargetGenerator.setGenerationTargetPath(OUT_BASE + "addComp/src");
        distributedTargetGenerator.add(new CPPGenImpl(TEST_PATH),"cpp");
        distributedTargetGenerator.add(new RclCppGenImpl(), "rclcpp");
        List<File> files = distributedTargetGenerator.generate(componentInstanceSymbol, taggingResolver);
    }

    @Test
    public void testBaSystem() throws IOException {
        TaggingResolver taggingResolver = createSymTabAndTaggingResolver(TEST_PATH);
        RosToEmamTagSchema.registerTagTypes(taggingResolver);

        EMAComponentInstanceSymbol componentInstanceSymbol = taggingResolver.<EMAComponentInstanceSymbol>resolve("ba.system", EMAComponentInstanceSymbol.KIND).orElse(null);
        assertNotNull(componentInstanceSymbol);

        TagHelper.resolveTags(taggingResolver, componentInstanceSymbol);

        DistributedTargetGenerator distributedTargetGenerator = new DistributedTargetGenerator();
        String generationTargetPath = OUT_BASE + "system/src/";
        distributedTargetGenerator.setGenerationTargetPath(generationTargetPath);
        //distributedTargetGenerator.setGenDebug(true);
        distributedTargetGenerator.add(new CPPGenImpl(TEST_PATH), "cpp");
        distributedTargetGenerator.add(new RclCppGenImpl(), "rclcpp");

        List<File> files = distributedTargetGenerator.generate(componentInstanceSymbol, taggingResolver);
    }

    @Test
    public void testDistributedTargetGenerator() throws IOException {
        TaggingResolver taggingResolver = createSymTabAndTaggingResolver(TEST_PATH);
        RosToEmamTagSchema.registerTagTypes(taggingResolver);

        EMAComponentInstanceSymbol componentInstanceSymbol = taggingResolver.<EMAComponentInstanceSymbol>resolve("tests.dist.distComp", EMAComponentInstanceSymbol.KIND).orElse(null);
        assertNotNull(componentInstanceSymbol);
        TagHelper.resolveTags(taggingResolver, componentInstanceSymbol);


        DistributedTargetGenerator distributedTargetGenerator = new DistributedTargetGenerator();
        distributedTargetGenerator.setGenerationTargetPath(OUT_BASE +  "distributed/src/");

        distributedTargetGenerator.add(new CPPGenImpl(TEST_PATH), "cpp");
        distributedTargetGenerator.add(new RclCppGenImpl(), "rclcpp");

        distributedTargetGenerator.generate(componentInstanceSymbol, taggingResolver);
    }

    @Test
    public void testDistributedStructTargetGenerator() throws IOException {
        TaggingResolver taggingResolver = createSymTabAndTaggingResolver(TEST_PATH);
        RosToEmamTagSchema.registerTagTypes(taggingResolver);

        EMAComponentInstanceSymbol componentInstanceSymbol = taggingResolver.<EMAComponentInstanceSymbol>resolve("tests.dist.distWithStructComp", EMAComponentInstanceSymbol.KIND).orElse(null);
        assertNotNull(componentInstanceSymbol);
        TagHelper.resolveTags(taggingResolver, componentInstanceSymbol);


        DistributedTargetGenerator distributedTargetGenerator = new DistributedTargetGenerator();
        distributedTargetGenerator.setGenerationTargetPath(OUT_BASE + "distributedStruct/src/");

        distributedTargetGenerator.add(new CPPGenImpl(TEST_PATH), "cpp");
        distributedTargetGenerator.add(new RclCppGenImpl(), "rclcpp");

        distributedTargetGenerator.generate(componentInstanceSymbol, taggingResolver);
    }
}
