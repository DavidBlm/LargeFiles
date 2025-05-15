package de.monticore.mlpipelines.automl.trainalgorithms.adanet.models;

import junit.framework.TestCase;


public class ParallelCandidateLayerTest extends TestCase {

    public void testConstructor() {
        ParallelCandidateLayer parallelCandidateLayer = new ParallelCandidateLayer();
        assertNotNull(parallelCandidateLayer);
    }

    public void testAddElement() {
        ParallelCandidateLayer parallelCandidateLayer = new ParallelCandidateLayer();
        ParallelCandidateLayerElement element = new ParallelCandidateLayerElement(1);
        parallelCandidateLayer.addElement(element);
        assertEquals(1, parallelCandidateLayer.getElements().size());
    }

    public void testGetElements() {
        ParallelCandidateLayer parallelCandidateLayer = new ParallelCandidateLayer();
        ParallelCandidateLayerElement element = new ParallelCandidateLayerElement(1);
        parallelCandidateLayer.addElement(element);
        assertEquals(1, parallelCandidateLayer.getElements().size());
    }
}