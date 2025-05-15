package de.monticore.mlpipelines.automl.emadlprinter;

import de.monticore.expressionsbasis._ast.ASTExpression;
import de.monticore.lang.math._ast.ASTNameExpression;
import de.monticore.lang.math._ast.ASTNumberExpression;
import de.monticore.lang.monticar.cnnarch._ast.*;
import de.monticore.lang.monticar.ranges._ast.ASTRange;
import de.monticore.lang.monticar.resolution._ast.ASTUnitNumberResolution;
import de.monticore.literals.literals._ast.ASTIntLiteral;
import de.monticore.literals.literals._ast.ASTNumericLiteral;
import de.monticore.literals.literals._ast.ASTStringLiteral;
import de.monticore.numberunit._ast.ASTNumberWithInf;
import de.monticore.numberunit._ast.ASTNumberWithUnit;
import de.monticore.prettyprint.IndentPrinter;

import java.util.List;
import java.util.Optional;

public class NumberPrinter {
    private final IndentPrinter printer;

    public NumberPrinter(IndentPrinter printer) {
        this.printer = printer;
    }

    public void printASTArchExpression(ASTArchExpression rhs) {
        ASTArchSimpleExpression expression = rhs.getExpression();
        printASTArchSimpleExpression(expression);
    }

    public void printASTRange(ASTRange range) {
        this.printer.print("(");
        printASTUnitNumberResolution(range.getMin());
        this.printer.print(":");
        printASTUnitNumberResolution(range.getMax());
        this.printer.print(")");
    }

    public void printASTExpression(ASTExpression expression) {
        if (expression instanceof ASTNumberExpression) {
            ASTNumberExpression numberExpression = (ASTNumberExpression) expression;
            printASTNumberExpression(numberExpression);
        } else if (expression instanceof ASTNameExpression) {
            ASTNameExpression nameExpression = (ASTNameExpression) expression;
            printASTNameExpression(nameExpression);
        }
    }

    private void printASTNumberExpression(ASTNumberExpression expression) {
        ASTNumberWithUnit numberWithUnit = expression.getNumberWithUnit();
        printASTNumberWithUnit(numberWithUnit);
    }

    private void printASTUnitNumberResolution(ASTUnitNumberResolution unitNumberResolution) {
        this.printASTNumberWithUnit(unitNumberResolution.getNumberWithUnit());
    }


    private void printASTArchSimpleExpression(ASTArchSimpleExpression expression) {
        if (expression.getTupleExpressionOpt().isPresent())
            printASTTupleExpression(expression.getTupleExpressionOpt().get());
        else if (expression.getStringOpt().isPresent()) {
            printStringNameExpression(expression.getStringOpt().get());
        } else if (expression.getArithmeticExpressionOpt().isPresent()) {
            ASTArchArithmeticExpression astArchArithmeticExpression = expression.getArithmeticExpressionOpt().get();
            if (astArchArithmeticExpression instanceof ASTArchSimpleArithmeticExpression)
                printASTSimpleArithmeticExpression((ASTArchSimpleArithmeticExpression) astArchArithmeticExpression);
        }
        //need to add booleanExpression condition as well
        //else if(expression.getBooleanExpression().isPresent()){}
    }

    private void printStringNameExpression(ASTStringLiteral astStringLiteral) {
        printer.print("\"" + astStringLiteral.getValue() +"\"");
    }

    private void printASTTupleExpression(ASTTupleExpression astTupleExpression) {
        List<ASTArchArithmeticExpression> expressions = astTupleExpression.getExpressionsList();
        printer.print("(");
        for (int i = 0; i < expressions.size(); i++) {
            printASTArithmeticExpression(expressions.get(i));
            if (i < expressions.size() - 1) {
                printer.print(",");
            }
        }
        printer.print(")");
    }

    private void printASTArithmeticExpression(ASTArchArithmeticExpression expression) {
        if (expression instanceof ASTArchSimpleArithmeticExpression)
            printASTSimpleArithmeticExpression((ASTArchSimpleArithmeticExpression) expression);
    }

    private void printASTSimpleArithmeticExpression(ASTArchSimpleArithmeticExpression expression) {
        Optional<ASTNumberExpression> numberExpressionOpt = expression.getNumberExpressionOpt();
        Optional<ASTNameExpression> nameExpressionOpt = expression.getNameExpressionOpt();

        if (numberExpressionOpt.isPresent())
            printASTNumberExpression(numberExpressionOpt.get());
        else if (nameExpressionOpt.isPresent())
            printASTNameExpression(nameExpressionOpt.get());
    }

    private void printASTNameExpression(ASTNameExpression astNameExpression) {
        String name = astNameExpression.getName();
        printer.print(name);

    }

    private void printASTNumberWithUnit(ASTNumberWithUnit numberWithUnit) {
        Optional<ASTNumberWithInf> numOpt = numberWithUnit.getNumOpt();
        if (numOpt.isPresent())
            printASTNumberWithInf(numOpt.get());
    }

    private void printASTNumberWithInf(ASTNumberWithInf num) {
        if (num.isPresentInf()) {
            printer.print("inf");
        } else {
            ASTNumericLiteral literal = num.getNumber();

            if (literal instanceof ASTIntLiteral) {
                ASTIntLiteral number = (ASTIntLiteral) literal;
                printer.print("" + number.getValue());
            }
        }
    }
}
