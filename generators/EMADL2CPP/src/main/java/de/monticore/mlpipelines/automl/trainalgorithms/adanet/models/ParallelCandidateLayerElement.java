package de.monticore.mlpipelines.automl.trainalgorithms.adanet.models;

public class ParallelCandidateLayerElement {
    private int units;

    public ParallelCandidateLayerElement(int units) {
        this.units = units;
    }

    public int getUnits() {
        return this.units;
    }

    public void setUnits(int units) {
        this.units = units;
    }
}
