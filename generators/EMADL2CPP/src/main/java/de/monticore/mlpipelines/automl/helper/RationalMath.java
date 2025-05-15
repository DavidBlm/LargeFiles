package de.monticore.mlpipelines.automl.helper;

import org.jscience.mathematics.number.Rational;

public class RationalMath {

    public static Rational scale(Rational rational, float scalingFactor) {
        float oldValue = getFloatValue(rational);
        float newValue = oldValue * scalingFactor;
        Rational newRational = of(newValue);
        return newRational;
    }

    public static float getFloatValue(Rational rational) {
        float dividend = rational.getDividend().floatValue();
        float divisor = rational.getDivisor().floatValue();
        return dividend / divisor;
    }

    public static Rational of(float value) {
        int precision = 10000;
        return of(value, precision);
    }

    public static Rational of(float value, int precision) {
        int dividend = Math.round(value * precision);
        return Rational.valueOf(dividend, precision);
    }

    public static Rational scaleRound(Rational rational, float scalingFactor) {
        float oldValue = getFloatValue(rational);
        int newValue = Math.round(oldValue * scalingFactor);
        return of(newValue);
    }

    public static Rational of(int value) {
        return Rational.valueOf(value, 1);
    }

    public static int getIntValue(Rational rational) {
        return rational.getDividend().intValue();
    }
}
