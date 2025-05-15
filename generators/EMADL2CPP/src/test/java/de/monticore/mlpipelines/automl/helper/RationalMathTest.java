package de.monticore.mlpipelines.automl.helper;

import junit.framework.TestCase;
import org.jscience.mathematics.number.Rational;

public class RationalMathTest extends TestCase {

    public void testFromWithInt() {
        Rational rational = RationalMath.of(10);
        assertEquals(10, RationalMath.getIntValue(rational));
    }

    public void testFromWithFloat() {
        Rational rational = RationalMath.of(9.5f);
        assertEquals(9.5f, RationalMath.getFloatValue(rational), 0.0001);
    }

    public void testFromWithPrecision() {
        Rational rational = RationalMath.of(9.05f, 10);
        assertEquals(9.1f, RationalMath.getFloatValue(rational), 0.0001);
    }

    public void testScale() {
        Rational rational = RationalMath.of(10);
        Rational scaledRational = RationalMath.scale(rational, 2);
        assertEquals(20, RationalMath.getIntValue(scaledRational));
    }

    public void testScaleRound() {
        Rational rational = RationalMath.of(10.3f);
        Rational scaledRational = RationalMath.scaleRound(rational, 2);
        assertEquals(21, RationalMath.getIntValue(scaledRational));
    }

    public void testGetFloatValue() {
        Rational rational = RationalMath.of(8.5f);
        assertEquals(8.5f, RationalMath.getFloatValue(rational), 0.0001);
    }

    public void testGetIntValue() {
        Rational rational = RationalMath.of(8);
        assertEquals(8, RationalMath.getIntValue(rational));
    }
}