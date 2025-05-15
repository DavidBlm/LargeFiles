package de.monticore.mlpipelines.tracking;

import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.LogStub;
import java.util.Arrays;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class ConfigurationTest {

    @BeforeClass
    public static void init() {
        LogStub.init();
    }

    @Before
    public void clearFindings() {
        Log.getFindings().clear();
    }

    @Test
    public void testNoConfiguration() {
        final TrackerFactory trackerFactory = new TrackerFactory(null);
        trackerFactory.createTracker();
        Assert.assertEquals(0, Log.getFindings().size());
    }

    @Test
    public void testValidConfiguration() {
        final TrackerFactory trackerFactory = new TrackerFactory("src/test/resources/tracking/configuration/valid/validConfiguration.conf");
        trackerFactory.createTracker();
        Assert.assertEquals(0, Log.getFindings().size());
    }

    @Test
    public void testInvalidConfigurations() {
        List<String> paths = Arrays.asList("src/test/resources/tracking/configuration/invalid/invalidBlacklist.conf",
                "src/test/resources/tracking/configuration/invalid/invalidTags1.conf",
                "src/test/resources/tracking/configuration/invalid/invalidTags2.conf",
                "src/test/resources/tracking/configuration/invalid/invalidTags3.conf");
        for(String path : paths) {
            TrackerFactory trackerFactory = new TrackerFactory(path);
            trackerFactory.createTracker();
            Assert.assertEquals(1, Log.getFindings().size());
            Log.getFindings().clear();
        }
    }

}
