package de.monticore.mlpipelines.tracking;

import de.monticore.mlpipelines.tracking.helper.SimpleRegexMatcher;
import de.monticore.mlpipelines.tracking.tracker.MultiBackendTracker;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.LogStub;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PersistentLoggingDataTest {

    @BeforeClass
    public static void init() {
        LogStub.init();
    }

    @Before
    public void clearFindings() {
        Log.getFindings().clear();
    }

    private MultiBackendTracker createMultiBackendTracker(RunTrackerStub stub) {
        return new MultiBackendTracker(Collections.singletonList(stub),
                new ArrayList<>(),
                new HashMap<>(),
                new SimpleRegexMatcher());
    }

    @Test
    public void testManualTags() {
        // Create manual tags
        Map<String, String> manualTags = new HashMap<>();
        manualTags.put("key1", "value1");
        manualTags.put("key2", "value2");

        RunTrackerStub stub = new RunTrackerStub();
        MultiBackendTracker tracker = new MultiBackendTracker(Collections.singletonList(stub),
                new ArrayList<>(),
                manualTags,
                new SimpleRegexMatcher());
        Map<String, String> stubTags = stub.getTags();

        // Start run and check if manual tags are there
        Assert.assertEquals(0, stubTags.size());
        tracker.startNewRun();
        Assert.assertEquals(2, stubTags.size());
        Assert.assertEquals("value1", stubTags.get("key1"));
        Assert.assertEquals("value2", stubTags.get("key2"));
        stubTags.clear();

        // Even after clearing persistent data, manual tags should be there
        tracker.clearPersistentData(); // Should clear persistent data and re-add manual tags
        tracker.startNewRun();
        Assert.assertEquals(2, stubTags.size());
        Assert.assertEquals("value1", stubTags.get("key1"));
        Assert.assertEquals("value2", stubTags.get("key2"));
        stubTags.clear();
    }

    @Test
    public void testPersistentTags() {
        RunTrackerStub stub = new RunTrackerStub();
        MultiBackendTracker tracker = createMultiBackendTracker(stub);
        Map<String, String> stubTags = stub.getTags();

        // Add persistent tags
        Map<String, String> tagsToAdd = new HashMap<>();
        tagsToAdd.put("key1", "value1");
        tagsToAdd.put("key2", "value2");
        tracker.addPersistentTags(tagsToAdd);

        // Start run and check if persistent tags are there
        Assert.assertEquals(0, stubTags.size());
        tracker.startNewRun();
        Assert.assertEquals(2, stubTags.size());
        Assert.assertEquals("value1", stubTags.get("key1"));
        Assert.assertEquals("value2", stubTags.get("key2"));
        stubTags.clear();

        // After clearing persistent data, persistent tags should be gone
        tracker.clearPersistentData();
        tracker.startNewRun();
        Assert.assertEquals(0, stubTags.size());
    }

    @Test
    public void testPersistentParams() {
        RunTrackerStub stub = new RunTrackerStub();
        MultiBackendTracker tracker = createMultiBackendTracker(stub);
        Map<String, String> stubParams = stub.getParams();

        // Add persistent tags
        Map<String, String> paramsToAdd = new HashMap<>();
        paramsToAdd.put("key1", "value1");
        paramsToAdd.put("key2", "value2");
        tracker.addPersistentParams(paramsToAdd);

        // Start run and check if persistent params are there
        Assert.assertEquals(0, stubParams.size());
        tracker.startNewRun();
        Assert.assertEquals(2, stubParams.size());
        Assert.assertEquals("value1", stubParams.get("key1"));
        Assert.assertEquals("value2", stubParams.get("key2"));
        stubParams.clear();

        // After clearing persistent data, persistent tags should be gone
        tracker.clearPersistentData();
        tracker.startNewRun();
        Assert.assertEquals(0, stubParams.size());
    }

    @Test
    public void testPersistentArtifacts() {
        RunTrackerStub stub = new RunTrackerStub();
        MultiBackendTracker tracker = createMultiBackendTracker(stub);
        Map<File, String> stubArtifacts = stub.getArtifacts();

        File testFile = new File("src/test/resources/tracking/testArtifact.emadl");

        // Only test file logging builder, since it is required for persistent artifact logging
        // Test Case 1: Simple persistent artifact
        tracker.getArtifactHandler().setArtifact(testFile).setFileName("newFileName").setPath("new/path").logPersistent();
        Assert.assertEquals(0, stubArtifacts.size()); // No artifact should be logged before startNewRun() is called
        tracker.startNewRun();
        Assert.assertEquals(1, stubArtifacts.size());
        Entry<File, String> entry = stubArtifacts.entrySet().stream().findFirst().get();
        Assert.assertEquals("newFileName", entry.getKey().getName());
        Assert.assertEquals("new/path", entry.getValue());
        stubArtifacts.clear();
        tracker.clearPersistentData();


        // Test Case 2: Persistent plaintext artifact
        tracker.getArtifactHandler().setPlaintext("Hello\nWorld").setFileName("plaintextArtifact").setPath("new/plaintext/path").logPersistent();
        Assert.assertEquals(0, stubArtifacts.size()); // No artifact should be logged before startNewRun() is called
        tracker.startNewRun();
        Assert.assertEquals(1, stubArtifacts.size());
        entry = stubArtifacts.entrySet().stream().findFirst().get();
        Assert.assertEquals("plaintextArtifact", entry.getKey().getName());
        Assert.assertEquals("new/plaintext/path", entry.getValue());
        stubArtifacts.clear();
        tracker.clearPersistentData();


        // Test Case 3: Multiple persistent artifacts
        tracker.getArtifactHandler().setArtifacts(Collections.singletonList(testFile)).addExtension(".txt").setPath("new/multiple/path").logPersistent();
        Assert.assertEquals(0, stubArtifacts.size()); // No artifact should be logged before startNewRun() is called
        tracker.startNewRun();
        Assert.assertEquals(1, stubArtifacts.size());
        entry = stubArtifacts.entrySet().stream().findFirst().get();
        Assert.assertEquals("testArtifact.emadl.txt", entry.getKey().getName());
        Assert.assertEquals("new/multiple/path", entry.getValue());
        stubArtifacts.clear();
        tracker.clearPersistentData();
    }

}
