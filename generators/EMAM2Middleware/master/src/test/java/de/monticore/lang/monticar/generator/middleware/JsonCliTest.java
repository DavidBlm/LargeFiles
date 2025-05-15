/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware;

import de.monticore.lang.monticar.generator.middleware.cli.DistributedTargetGeneratorCli;
import org.junit.Ignore;
import org.junit.Test;

@Ignore("Not a test, does not assert anything")
public class JsonCliTest {

    @Test
    public void testClusteringFlattenSpectral() {
        DistributedTargetGeneratorCli.main(new String[]{"./src/test/resources/config/parameterTest/clusterParamsFlattenSpectral.json"});
    }

    @Test
    public void testClusteringFlattenSpectralMulti() {
        DistributedTargetGeneratorCli.main(new String[]{"./src/test/resources/config/parameterTest/clusterParamsFlattenSpectralMulti.json"});
    }

    @Test
    public void testClusteringFlattenMarkov() {
        DistributedTargetGeneratorCli.main(new String[]{"./src/test/resources/config/parameterTest/clusterParamsFlattenMarkov.json"});
    }

    @Test
    public void testClusteringFlattenAffProp() {
        DistributedTargetGeneratorCli.main(new String[]{"./src/test/resources/config/parameterTest/clusterParamsFlattenAffProp.json"});
    }

    @Ignore("DBScan failing")
    @Test
    public void testClusteringFlattenDBScan() {
        DistributedTargetGeneratorCli.main(new String[]{"./src/test/resources/config/parameterTest/clusterParamsFlattenDBScan.json"});
    }

    @Ignore("DBScan failing")
    @Test
    public void testClusteringAllAlgosMinParams() {
        DistributedTargetGeneratorCli.main(new String[]{"./src/test/resources/config/parameterTest/clusterParamsAllAlgosMinParams.json"});
    }

    @Test
    public void testDynamicParameterList() {
        DistributedTargetGeneratorCli.main(new String[]{"./src/test/resources/config/parameterTest/clusterDynamicList.json"});
    }


}
