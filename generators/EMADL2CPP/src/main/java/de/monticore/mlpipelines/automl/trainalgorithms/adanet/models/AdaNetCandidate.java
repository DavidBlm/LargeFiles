package de.monticore.mlpipelines.automl.trainalgorithms.adanet.models;

import java.util.ArrayList;
import java.util.List;

public class AdaNetCandidate {
    private AdaNetComponent component;
    private List<AdaNetComponent> previousComponents;

    public AdaNetCandidate() {
    }

    public AdaNetCandidate(AdaNetComponent component, List<AdaNetComponent> previousComponents) {
        this.component = component;
        this.previousComponents = previousComponents;

        if (previousComponents == null) {
            this.previousComponents = new ArrayList<>();
        }
    }

    public AdaNetComponent getComponent() {
        return this.component;
    }

    public void setComponent(AdaNetComponent component) {
        this.component = component;
    }

    public List<AdaNetComponent> getPreviousComponents() {
        return this.previousComponents;
    }

    public void setPreviousComponents(List<AdaNetComponent> previousComponents) {
        this.previousComponents = previousComponents;
    }

    public List<ParallelCandidateLayer> getParallelCandidateLayers() {
        List<ParallelCandidateLayer> layers = new ArrayList<>();
        for (int layerIndex = 0; layerIndex < getMaxDepth(); layerIndex++) {
            ParallelCandidateLayer layer = createParallelCandidateLayer(layerIndex);
            layers.add(layer);
        }
        return layers;
    }

    public List<AdaNetComponent> getAllComponents() {
        List<AdaNetComponent> allComponents = new ArrayList<>();
        allComponents.addAll(this.previousComponents);
        allComponents.add(this.component);
        return allComponents;
    }

    private ParallelCandidateLayer createParallelCandidateLayer(int layerIndex) {
        ParallelCandidateLayer layer = new ParallelCandidateLayer();
        for (AdaNetComponent component : this.getAllComponents()) {
            if (component.getDepth() >= layerIndex + 1) {
                int units = component.getLayerWidth();
                ParallelCandidateLayerElement element = new ParallelCandidateLayerElement(units);
                layer.addElement(element);
            }
        }
        return layer;
    }

    private int getMaxDepth() {
        return component.getDepth();
    }
}
