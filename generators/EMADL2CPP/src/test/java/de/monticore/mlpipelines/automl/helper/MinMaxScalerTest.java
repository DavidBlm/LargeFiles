package de.monticore.mlpipelines.automl.helper;

import junit.framework.TestCase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;

@RunWith(MockitoJUnitRunner.class)
public class MinMaxScalerTest extends TestCase {

    private double[][] x;

    @Before
    public void setup() throws IOException {
        x = new double[][]{{1, 0.0001, 32}, {10, 0.001, 64}, {3, 0.01, 16}, {6, 0.1, 128}};
    }

    @Test
    public void testNormalizeArr() {
        double[][] normalizedArr = MinMaxScaler.normalizeArr(x, -1.0, 1.0);
        assertTrue(this.allValsBetween(normalizedArr, -1.0, 1.0));
    }

    private boolean allValsBetween(double[][] arr, double min, double max) {
        boolean valsBetween = true;
        for (int i=0; i < arr.length; i++) {
            for (int j=0; j < arr[0].length; j++) {
                valsBetween &= (arr[i][j] >= min) && (arr[i][j] <= max);
            }
        }
        return valsBetween;
    }
}
