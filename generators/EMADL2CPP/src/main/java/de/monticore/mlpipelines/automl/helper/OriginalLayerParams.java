package de.monticore.mlpipelines.automl.helper;

public class OriginalLayerParams {
    private int originalDepthValue;
    private int originalChannelValue;
    static int imageDimensionValue = 0;

    public OriginalLayerParams(int originalDepthValue, int originalChannelValue) {
        this.originalDepthValue = originalDepthValue;
        this.originalChannelValue = originalChannelValue;
    }

    public int getOriginalChannelValue() {
        return originalChannelValue;
    }

    public int getOriginalDepthValue() {
        return originalDepthValue;
    }

    public static void changeImageDimensionValue(int dimension){
        imageDimensionValue = dimension;
    }

    public static int getImageDimensionValue(){
        return imageDimensionValue;
    }
}
