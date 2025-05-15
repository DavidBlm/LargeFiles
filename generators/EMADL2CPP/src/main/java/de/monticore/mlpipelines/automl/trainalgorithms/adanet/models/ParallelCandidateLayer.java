package de.monticore.mlpipelines.automl.trainalgorithms.adanet.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Is used to save elements which should run in parallel
 */
public class ParallelCandidateLayer {
    private final List<ParallelCandidateLayerElement> elements;

    public ParallelCandidateLayer() {
        this.elements = new ArrayList<>();
    }

    public void addElement(ParallelCandidateLayerElement element) {
        this.elements.add(element);
    }

    public List<ParallelCandidateLayerElement> getElements() {
        return this.elements;
    }
}
