/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAPortInstanceSymbol;
import de.monticore.lang.monticar.clustering.AutomaticClusteringHelper;
import de.monticore.lang.monticar.clustering.ClusteringInput;
import de.monticore.lang.monticar.clustering.FlattenArchitecture;
import de.monticore.lang.monticar.clustering.algorithms.SpectralClusteringAlgorithm;
import de.monticore.lang.monticar.clustering.algorithms.SpectralClusteringBuilder;
import de.monticore.lang.monticar.generator.middleware.impls.CPPGenImpl;
import de.monticore.lang.monticar.generator.middleware.impls.RosCppGenImpl;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AutomaticClusteringTest extends AbstractSymtabTest{

    public static final String TEST_PATH = "src/test/resources/";
    public static final String TEST_PATH_PNG = "src/test/resources/clustering/test-images/";
    public static final String TEST_PATH_PACMAN = "src/test/resources/pacman/";

    @Test
    public void testPacmanModelClustering() throws IOException {
        TaggingResolver taggingResolver = AbstractSymtabTest.createSymTabAndTaggingResolver(TEST_PATH_PACMAN);
        EMAComponentInstanceSymbol componentInstanceSymbol = taggingResolver.<EMAComponentInstanceSymbol>resolve("de.rwth.pacman.heithoff2.controller", EMAComponentInstanceSymbol.KIND).orElse(null);
        assertNotNull(componentInstanceSymbol);

        EMAComponentInstanceSymbol flattendComponent = FlattenArchitecture.flattenArchitecture(componentInstanceSymbol);

        SpectralClusteringAlgorithm clusteringAlgorithm = new SpectralClusteringAlgorithm();
        Object[] params = new Object[]{SpectralClusteringBuilder.SpectralParameters.SPECTRAL_NUM_CLUSTERS, 2};
        List<Set<EMAComponentInstanceSymbol>> clusters = clusteringAlgorithm.cluster(new ClusteringInput(flattendComponent), params);

        AutomaticClusteringHelper.annotateComponentWithRosTagsForClusters(flattendComponent, clusters);

        DistributedTargetGenerator distributedTargetGenerator = new DistributedTargetGenerator();
        distributedTargetGenerator.setGenerateMiddlewareTags(true);
        distributedTargetGenerator.setGenerationTargetPath("target/generated-sources-clustering/pacman/src/");

        distributedTargetGenerator.add(new CPPGenImpl(TEST_PATH_PACMAN), "cpp");
        distributedTargetGenerator.add(new RosCppGenImpl(),"roscpp");

        distributedTargetGenerator.generate(flattendComponent,taggingResolver);
    }


    @Test
    public void testClusterToRosConnections() throws IOException {
        TaggingResolver taggingResolver = AbstractSymtabTest.createSymTabAndTaggingResolver(TEST_PATH);

        //ClustersWithSingleConnection
        EMAComponentInstanceSymbol componentInstanceSymbol = taggingResolver.<EMAComponentInstanceSymbol>resolve("clustering.clustersWithSingleConnection", EMAComponentInstanceSymbol.KIND).orElse(null);
        assertNotNull(componentInstanceSymbol);

        //Force cluster; spectral would not cluster this way!
        List<Set<EMAComponentInstanceSymbol>> clusters = new ArrayList<>();

        HashSet<EMAComponentInstanceSymbol> cluster1 = new HashSet<>();
        cluster1.add(componentInstanceSymbol.getSubComponent("outComp1").get());
        cluster1.add(componentInstanceSymbol.getSubComponent("inOutComp").get());
        clusters.add(cluster1);

        HashSet<EMAComponentInstanceSymbol> cluster2 = new HashSet<>();
        cluster2.add(componentInstanceSymbol.getSubComponent("outComp2").get());
        cluster2.add(componentInstanceSymbol.getSubComponent("doubleInComp").get());
        clusters.add(cluster2);


        AutomaticClusteringHelper.annotateComponentWithRosTagsForClusters(componentInstanceSymbol, clusters);

        List<String> rosPortsSuper = componentInstanceSymbol.getPortInstanceList().stream()
                .filter(EMAPortInstanceSymbol::isRosPort)
                .map(EMAPortInstanceSymbol::getFullName)
                .collect(Collectors.toList());

        List<String> rosPortsSubComps = componentInstanceSymbol.getSubComponents().stream()
                .flatMap(subc -> subc.getPortInstanceList().stream())
                .filter(EMAPortInstanceSymbol::isRosPort)
                .map(EMAPortInstanceSymbol::getFullName)
                .collect(Collectors.toList());

        //No Ports in super comp
        assertTrue(rosPortsSuper.size() == 0);
        assertTrue(rosPortsSubComps.size() == 2);

        assertTrue(rosPortsSubComps.contains("clustering.clustersWithSingleConnection.inOutComp.out1"));
        assertTrue(rosPortsSubComps.contains("clustering.clustersWithSingleConnection.doubleInComp.in2"));

        DistributedTargetGenerator distributedTargetGenerator = new DistributedTargetGenerator();
        distributedTargetGenerator.setGenerationTargetPath("./target/generated-sources-clustering/ClusterToRosConnections/src/");

        distributedTargetGenerator.add(new CPPGenImpl(TEST_PATH), "cpp");
        distributedTargetGenerator.add(new RosCppGenImpl(),"roscpp");

        distributedTargetGenerator.generate(componentInstanceSymbol,taggingResolver);
    }

}
