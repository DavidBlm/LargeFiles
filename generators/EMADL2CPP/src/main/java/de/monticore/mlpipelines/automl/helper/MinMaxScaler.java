package de.monticore.mlpipelines.automl.helper;

public class MinMaxScaler {

    public static double[][] normalizeArr(double[][] x, double min, double max) {
        double[][] normalizedArr = x.clone();
        for (int i=0; i < x[0].length; i++) {
            double[] featureArr = getColumn(x, i);
            double rMin = getMin(featureArr);
            double rMax = getMax(featureArr);
            for (int j=0; j < featureArr.length; j++) {
                double normalizedVal = normalizeVal(rMin, rMax, min, max, featureArr[j]);
                normalizedArr[j][i] = normalizedVal;
            }
        }
        return normalizedArr;
    }

    private static double normalizeVal(double rMin, double rMax, double tMin, double tMax, double val) {
        if (rMin == rMax) {
            return (tMax + tMin) / 2;
        } else {
            return ((val - rMin) / (rMax - rMin)) * (tMax - tMin) + tMin;
        }
    }

    private static double getMax(double[] arr) {
        double max = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] > max) {
                max = arr[i];
            }
        }
        return max;
    }

    private static double getMin(double[] arr) {
        double min = Double.POSITIVE_INFINITY;
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] < min) {
                min = arr[i];
            }
        }
        return min;
    }

    private static double[] getColumn(double[][] arr, int colIndex) {
        double[] colArr = new double[arr.length];
        for (int i=0; i < colArr.length; i++) {
            colArr[i] = arr[i][colIndex];
        }
        return colArr;
    }
}
