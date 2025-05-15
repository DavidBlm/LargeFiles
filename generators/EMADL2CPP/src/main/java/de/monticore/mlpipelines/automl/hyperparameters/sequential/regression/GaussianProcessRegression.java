package de.monticore.mlpipelines.automl.hyperparameters.sequential.regression;

import org.apache.commons.math3.linear.*;

import java.util.HashMap;
import java.util.Map;

public class GaussianProcessRegression {

    private double noise = 0;

    private double[][] xTrain;

    private RealMatrix gaussianKernel;

    private RealVector alpha;

    public GaussianProcessRegression() {
    }

    public GaussianProcessRegression(double noise) {
        this.noise = noise;
    }

    public void fit(double[][] x, double[] y) {
        RealMatrix noiseMatrix = MatrixUtils.createRealIdentityMatrix(x.length).scalarMultiply(Math.pow(this.noise, 2));
        this.gaussianKernel = this.getGaussianKernel(x, x).add(noiseMatrix);
        this.alpha = this.choleskySolve(this.gaussianKernel, y);
        this.xTrain = x;
    }

    public Map<String, Object> predict(double[][] x) {
        Map<String, Object> predMap = new HashMap<>();
        double[] yMean = this.predMean(x);
        double[][] yCov = this.predCov(x);

        predMap.put("mean", yMean);
        predMap.put("cov", yCov);
        return predMap;
    }

    private double[] predMean(double[][] x) {
        RealMatrix kStar = this.getGaussianKernel(this.xTrain, x);
        RealVector yMean = kStar.preMultiply(this.alpha);
        return ((ArrayRealVector)yMean).getDataRef();
    }

    private double[][] predCov(double[][] x) {
        RealMatrix kStar = this.getGaussianKernel(this.xTrain, x);
        RealMatrix kStarT = kStar.transpose();
        LUDecomposition luDecomposition = new LUDecomposition(this.gaussianKernel);
        RealMatrix v = luDecomposition.getSolver().solve(kStar);
        RealMatrix yCov = this.getGaussianKernel(x, x).subtract(kStarT.multiply(v));
        return yCov.getData();
    }

    private double squaredDistance(double[] x, double[] y) {
        if (x.length != y.length) {
            throw new IllegalArgumentException("Input vector sizes are different.");
        } else {
            double sum = 0.0;
            for(int i = 0; i < x.length; i++) {
                double d = x[i] - y[i];
                sum += d * d;
            }
            return sum;
        }
    }

    private double calcKernelEntry(double dist) {
        return Math.exp(-0.5 * dist);
    }

    private RealMatrix getGaussianKernel(double[][] x1, double[][] x2) {
        int n = x1.length;
        int m = x2.length;
        double[][] K = new double[n][m];
        for (int i = 0; i < n; i++) {
            for (int j=0; j < m; j++) {
                double dist = this.squaredDistance(x1[i], x2[j]);
                K[i][j] = this.calcKernelEntry(dist);
            }
        }
        return MatrixUtils.createRealMatrix(K);
    }

    private RealVector choleskySolve(RealMatrix K, double[] y) {
        RealVector yVec = MatrixUtils.createRealVector(y);
        CholeskyDecomposition decomposition = new CholeskyDecomposition(K);
        return decomposition.getSolver().solve(yVec);
    }
}
