package de.monticore.mlpipelines.automl.helper;

import de.monticore.lang.math._symboltable.expression.MathNumberExpressionSymbol;
import org.jscience.mathematics.number.Rational;

public class MathNumberExpressionWrapper {
    private final MathNumberExpressionSymbol number;

    public MathNumberExpressionWrapper(MathNumberExpressionSymbol numberSymbol) {
        this.number = numberSymbol;
    }

    public void setValue(int value) {
        Rational newValue = RationalMath.of(value);
        number.getValue().setRealNumber(newValue);
    }

    public void setValue(float value) {
        int precision = 10000;
        setValue(value, precision);
    }

    public void setValue(float value, int divisor) {
        Rational newValue = RationalMath.of(value, divisor);
        number.getValue().setRealNumber(newValue);
    }

    public void scale(float scalingFactor) {
        float oldValue = getFloatValue();
        float newValue = oldValue * scalingFactor;
        setValue(newValue);
    }

    public void scaleRound(float scalingFactor) {
        float oldValue = getFloatValue();
        int newValue = Math.round(oldValue * scalingFactor);
        setValue(newValue);
    }

    public float getFloatValue() {
        Rational rational = number.getValue().getRealNumber();
        return RationalMath.getFloatValue(rational);
    }

    public int getIntValue() {
        Rational rational = number.getValue().getRealNumber();
        return RationalMath.getIntValue(rational);
    }
}
