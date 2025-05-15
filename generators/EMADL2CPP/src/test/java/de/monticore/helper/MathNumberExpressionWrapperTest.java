package de.monticore.helper;

import de.monticore.lang.math._symboltable.expression.MathNumberExpressionSymbol;
import de.monticore.mlpipelines.automl.helper.MathNumberExpressionWrapper;
import junit.framework.TestCase;
import org.jscience.mathematics.number.Rational;

public class MathNumberExpressionWrapperTest extends TestCase {

    public void testConstructor(){
        MathNumberExpressionSymbol mathNumberExpressionSymbol = getMathNumberExpressionSymbol(1, 1);
        MathNumberExpressionWrapper wrapper = new MathNumberExpressionWrapper(mathNumberExpressionSymbol);
        assertNotNull(wrapper);
    }

    public void testSetValueInt() {
        MathNumberExpressionSymbol mathNumberExpressionSymbol = getMathNumberExpressionSymbol(1, 1);
        MathNumberExpressionWrapper wrapper = new MathNumberExpressionWrapper(mathNumberExpressionSymbol);
        wrapper.setValue(2);
        assertEquals(2, wrapper.getFloatValue(), 0.0001);
    }

    public void testTestSetValueFloat() {
        MathNumberExpressionSymbol mathNumberExpressionSymbol = getMathNumberExpressionSymbol(1, 1);
        MathNumberExpressionWrapper wrapper = new MathNumberExpressionWrapper(mathNumberExpressionSymbol);
        wrapper.setValue(2.0f);
        assertEquals(2.0f, wrapper.getFloatValue(), 0.0001);
    }

    public void testTestSetValueFloatWithPrecision() {
        MathNumberExpressionSymbol mathNumberExpressionSymbol = getMathNumberExpressionSymbol(1, 1);
        MathNumberExpressionWrapper wrapper = new MathNumberExpressionWrapper(mathNumberExpressionSymbol);
        wrapper.setValue(2.0f, 100000);
        assertEquals(2.0f, wrapper.getFloatValue(), 0.0001);
    }

    public void testGetFloatValue() {
        MathNumberExpressionSymbol mathNumberExpressionSymbol = getMathNumberExpressionSymbol(1, 1);
        MathNumberExpressionWrapper wrapper = new MathNumberExpressionWrapper(mathNumberExpressionSymbol);
        assertEquals(1.0f, wrapper.getFloatValue(), 0.0001);
    }

    public void testGetIntValue() {
        MathNumberExpressionSymbol mathNumberExpressionSymbol = getMathNumberExpressionSymbol(1, 1);
        MathNumberExpressionWrapper wrapper = new MathNumberExpressionWrapper(mathNumberExpressionSymbol);
        assertEquals(1, wrapper.getIntValue());
    }

    public void testScale() {
        MathNumberExpressionSymbol mathNumberExpressionSymbol = getMathNumberExpressionSymbol(2, 1);
        MathNumberExpressionWrapper wrapper = new MathNumberExpressionWrapper(mathNumberExpressionSymbol);
        float scalingFactor = 2;
        wrapper.scale(scalingFactor);
        assertEquals(4.0f, wrapper.getFloatValue(), 0.0001);
    }

    private static MathNumberExpressionSymbol getMathNumberExpressionSymbol(int dividend, int divisor) {
        Rational rational = Rational.valueOf(dividend, divisor);
        MathNumberExpressionSymbol mathNumberExpressionSymbol = new MathNumberExpressionSymbol(rational);
        return mathNumberExpressionSymbol;
    }
}