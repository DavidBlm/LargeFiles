package de.monticore.mlpipelines.automl.hyperparameters.sequential.regression;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertArrayEquals;

@RunWith(MockitoJUnitRunner.class)
public class GaussianProcessRegressionTest extends TestCase {

    private double[][] xTrain;
    private double[] yTrain;

    private double[] func(double[][] x) {
        double[] y = new double[x.length];
        for (int i=0; i < x.length; i++) {
            y[i] = Math.pow(x[i][0], 2) + Math.pow(x[i][1], 3);
        }
        return y;
    }

    private double[][] getXTrain() {
        double[][] xTrain = new double[10][2];
        for (int i=0; i < xTrain.length; i++) {
            xTrain[i][0] = i - 5;
            xTrain[i][1] = i - 5;
        }
        return xTrain;
    }

    @Before
    public void setup() throws IOException {
        xTrain = this.getXTrain();
        yTrain = this.func(xTrain);
    }

    @Test
    public void testFitPredict() {
        GaussianProcessRegression gpr = new GaussianProcessRegression(1);
        gpr.fit(xTrain, yTrain);

        double[][] xTest = {
                {2.5, 2.5}
        };
        Map<String, Object> prediction = gpr.predict(xTest);

        assertArrayEquals((double[]) prediction.get("mean"), new double[]{14.959492721793671}, 0.0);
        assertArrayEquals(((double[][]) prediction.get("cov"))[0], new double[]{0.48721873563147955}, 0.0);
    }
}
