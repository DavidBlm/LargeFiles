package de.monticore.mlpipelines.automl.trainalgorithms.efficientnet;

public class ScalingFactors {

    public double alpha;  //depth

    public double beta; //width
    public double gamma; //resolution

    public ScalingFactors(double alpha, double beta, double gamma) {
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;
    }

    @Override
    public String toString() {
        //Only show with two decimal places:
        return "alpha: " + String.format("%.2f", alpha) +
                ", beta: " + String.format("%.2f", beta) +
                ", gamma: " + String.format("%.2f", gamma);
    }
}
