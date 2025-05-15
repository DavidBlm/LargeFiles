/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.clustering.FlattenArchitecture;
import de.monticore.lang.monticar.clustering.Simulation.MonteCarloIntegration;
import de.monticore.lang.monticar.clustering.Simulation.MonteCarloKargerStrategy;
import de.monticore.lang.monticar.clustering.Simulation.MonteCarloResult;
import de.monticore.lang.monticar.generator.middleware.cli.DistributedTargetGeneratorCli;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

@Ignore("Used for evaluation, nothing gets asserted")
public class EvaluationTest {

    @Test
    public void testPacman() {
        DistributedTargetGeneratorCli.main(new String[]{"./src/test/resources/config/evaluation/pacman.json"});
    }

    @Test
    public void testPacmanSilhouette() {
        DistributedTargetGeneratorCli.main(new String[]{"./src/test/resources/config/evaluation/pacmanSilhouette.json"});
    }

    @Test
    public void testAutopilot() {
        DistributedTargetGeneratorCli.main(new String[]{"./src/test/resources/config/evaluation/autopilot.json"});
    }

    @Test
    public void testAutopilotSilhouette() {
        DistributedTargetGeneratorCli.main(new String[]{"./src/test/resources/config/evaluation/autopilotSilhouette.json"});
    }

    @Test
    public void testSupermario() {
        DistributedTargetGeneratorCli.main(new String[]{"./src/test/resources/config/evaluation/supermario.json"});
    }

    @Test
    public void testSupermarioSilhouette() {
        DistributedTargetGeneratorCli.main(new String[]{"./src/test/resources/config/evaluation/supermarioSilhouette.json"});
    }

    @Test
    public void testDaimlerModel() {
        DistributedTargetGeneratorCli.main(new String[]{"./src/test/resources/config/evaluation/daimler.json"});
    }

    @Test
    public void testDaimlerModelSilhouette() {
        DistributedTargetGeneratorCli.main(new String[]{"./src/test/resources/config/evaluation/daimlerSilhouette.json"});
    }

    @Test
    public void testPacmanMonteCarlo() {
        String modelPath = "src/test/resources/pacman/";
        String compName = "de.rwth.pacman.heithoff2.controller";
        String shortName = "pacman";
        MonteCarloSimulation(modelPath, compName, shortName, 1000);
    }

    @Test
    public void testAutopilotMonteCarlo() {
        String modelPath = "src/test/resources/autopilot/";
        String compName = "de.rwth.armin.modeling.autopilot.autopilot";
        String shortName = "autopilot";
        MonteCarloSimulation(modelPath, compName, shortName, 1000);
    }

    @Test
    public void testSupermarioMonteCarlo() {
        String modelPath = "src/test/resources/supermario/";
        String compName = "de.rwth.supermario.haller.controller";
        String shortName = "supermario";
        MonteCarloSimulation(modelPath, compName, shortName, 1000);
    }

    @Test
    public void testDaimlerMonteCarlo() {
        String modelPath = "src/test/resources/daimlerModel/";
        String compName = "daimler.v4.oeffentlicher_Demonstrator_FAS_v04";
        String shortName = "daimler";
        MonteCarloSimulation(modelPath, compName, shortName, 100);
    }


    protected void MonteCarloSimulation(String modelPath, String compName, String shortName, int iterations) {
        TaggingResolver taggingResolver = AbstractSymtabTest.createSymTabAndTaggingResolver(modelPath);
        EMAComponentInstanceSymbol componentInstanceSymbol = taggingResolver.<EMAComponentInstanceSymbol>resolve(compName, EMAComponentInstanceSymbol.KIND).orElse(null);
        assertNotNull(componentInstanceSymbol);
        EMAComponentInstanceSymbol flattenedComponent = FlattenArchitecture.flattenArchitecture(componentInstanceSymbol);

        //Random Clustering
        System.out.println("Starting Simulation");
        MonteCarloIntegration sim = new MonteCarloIntegration(iterations, 3);
        sim.setClusteringDelegate(new MonteCarloKargerStrategy());
        sim.simulate(flattenedComponent);

        MonteCarloResult res = new MonteCarloResult(componentInstanceSymbol, sim);
        res.saveAsJson("target/evaluation/" + shortName + "MC/", "monteCarloResults.json");
    }
}
