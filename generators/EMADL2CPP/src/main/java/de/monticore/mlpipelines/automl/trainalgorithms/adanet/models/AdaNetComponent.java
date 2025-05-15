package de.monticore.mlpipelines.automl.trainalgorithms.adanet.models;

import de.monticore.mlpipelines.automl.configuration.AdaNetConfig;

public class AdaNetComponent {
    private int depth;
    private int layerWidth;

    public AdaNetComponent(int depth) {
        this.depth = depth;
        this.layerWidth = AdaNetConfig.UNITS_PER_LAYER;
    }

    public AdaNetComponent(int depth, int layerWidth) {
        this.depth = depth;
        this.layerWidth = layerWidth;
    }

    public int getDepth() {
        return this.depth;
    }

    public int getLayerWidth() {
        return this.layerWidth;
    }

    public int setLayerWidth(int layerWidth) {
        return this.layerWidth = layerWidth;
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }
}
