package de.monticore.mlpipelines.automl.trainalgorithms.adanet.models;

import junit.framework.TestCase;

public class ParallelCandidateLayerElementTest extends TestCase {

    public void testConstructor() {
        ParallelCandidateLayerElement parallelCandidateLayerElement = new ParallelCandidateLayerElement(1);
        assertNotNull(parallelCandidateLayerElement);
    }

    public void testGetUnits() {
        ParallelCandidateLayerElement parallelCandidateLayerElement = new ParallelCandidateLayerElement(1);
        assertEquals(1, parallelCandidateLayerElement.getUnits());
    }

    public void testSetUnits() {
        ParallelCandidateLayerElement parallelCandidateLayerElement = new ParallelCandidateLayerElement(1);
        parallelCandidateLayerElement.setUnits(2);
        assertEquals(2, parallelCandidateLayerElement.getUnits());
    }
}