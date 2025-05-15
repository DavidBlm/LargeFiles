package de.monticore.mlpipelines.automl.trainalgorithms;

import de.monticore.lang.math._ast.ASTNumberExpression;
import de.monticore.lang.math._ast.ASTNumberExpressionBuilder;
import de.monticore.lang.math._ast.MathMill;
import de.monticore.lang.math._symboltable.expression.MathNumberExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._ast.*;
import de.monticore.lang.monticar.cnnarch._symboltable.*;
import de.monticore.literals.literals._ast.ASTIntLiteral;
import de.monticore.literals.literals._ast.ASTIntLiteralBuilder;
import de.monticore.mlpipelines.automl.helper.RationalMath;
import de.monticore.numberunit._ast.ASTNumberWithInf;
import de.monticore.numberunit._ast.ASTNumberWithInfBuilder;
import de.monticore.numberunit._ast.ASTNumberWithUnit;
import de.monticore.numberunit._ast.ASTNumberWithUnitBuilder;

import java.util.ArrayList;
import java.util.List;

public class ASTGenerator {
    public static ASTParallelBlock createParallelBlockForSymbol(ParallelCompositeElementSymbol element) {
        ASTParallelBlockBuilder builder = CNNArchMill.parallelBlockBuilder();
        builder.setSymbol(element);
        for (ArchitectureElementSymbol elementSymbol : element.getElements()) {
            if (elementSymbol instanceof SerialCompositeElementSymbol) {
                ASTStream elementGroup = createStreamForSymbol((SerialCompositeElementSymbol) elementSymbol);
                builder.addGroups(elementGroup);
            }
        }
        ASTParallelBlock ast = builder.build();
        element.setAstNode(ast);
        return ast;
    }

    public static ASTStream createStreamForSymbol(SerialCompositeElementSymbol symbol) {
        ASTStreamBuilder builder = CNNArchMill.streamBuilder();
        builder.setSymbol(symbol);
        for (ArchitectureElementSymbol element : symbol.getElements()) {
            if (element instanceof ParallelCompositeElementSymbol) {
                ParallelCompositeElementSymbol castedElement = (ParallelCompositeElementSymbol) element;
                ASTParallelBlock elementGroup = createParallelBlockForSymbol(castedElement);
                builder.addElements(elementGroup);
            } else if (element instanceof LayerSymbol) {
                LayerSymbol castedElement = (LayerSymbol) element;
                ASTLayer layer = ASTGenerator.createLayerForElement(castedElement);
                builder.addElements(layer);
            }
        }
        ASTStream ast = builder.build();
        symbol.setAstNode(ast);
        return ast;
    }

    public static ASTLayer createLayerForElement(LayerSymbol element) {
        ASTLayerBuilder builder = CNNArchMill.layerBuilder();
        builder.setSymbol(element);
        builder.setName(element.getName());
        List<ASTArchArgument> argumentAsts = getArchArguments(element);
        builder.addAllArgumentss(argumentAsts);

        ASTLayer ast = builder.build();
        element.setAstNode(ast);
        return ast;
    }

    private static List<ASTArchArgument> getArchArguments(LayerSymbol element) {
        List<ASTArchArgument> argumentAsts = new ArrayList<>();
        List<ArgumentSymbol> argumentSymbols = element.getArguments();
        if (argumentSymbols != null) {
            for (ArgumentSymbol argumentSymbol : argumentSymbols) {
                ASTArchParameterArgument argumentAst = ASTGenerator.createArgument(argumentSymbol);
                argumentAsts.add(argumentAst);
            }
        }
        return argumentAsts;
    }

    public static ASTArchParameterArgument createArgument(ArgumentSymbol symbol) {
        ASTArchParameterArgumentBuilder builder = CNNArchMill.archParameterArgumentBuilder();
        builder.setName(symbol.getName());
        builder.setSymbol(symbol);
        ASTArchExpression archExpression = createArchExpression(symbol.getRhs());
        builder.setRhs(archExpression);

        ASTArchParameterArgument ast = builder.build();
        symbol.setAstNode(ast);
        return ast;
    }

    public static ASTArchExpression createArchExpression(ArchExpressionSymbol symbol) {
        if (symbol instanceof ArchSimpleExpressionSymbol) {
            ArchSimpleExpressionSymbol symbolCasted = (ArchSimpleExpressionSymbol) symbol;
            ASTArchExpressionBuilder builder = CNNArchMill.archExpressionBuilder();
            builder.setSymbol(symbol);
            MathNumberExpressionSymbol mathExpressionSymbol =
                    (MathNumberExpressionSymbol) symbolCasted.getMathExpression().get();
            ASTArchSimpleExpression mathExpression = createMathNumberExpression(mathExpressionSymbol);
            builder.setExpression(mathExpression);
            ASTArchExpression ast = builder.build();
            symbol.setAstNode(ast);
            return ast;
        }

        throw new UnsupportedOperationException("This type of ArchExpressionSymbol is not supported yet.");
    }

    public static ASTArchSimpleExpression createMathNumberExpression(MathNumberExpressionSymbol symbol) {
        ASTArchSimpleExpressionBuilder builder = CNNArchMill.archSimpleExpressionBuilder();
        builder.setSymbol(symbol);
        ASTArchSimpleArithmeticExpression arithmeticExpression = createArithmeticExpression(symbol);
        builder.setArithmeticExpression(arithmeticExpression);
        ASTArchSimpleExpression ast = builder.build();
        symbol.setAstNode(ast);
        return ast;
    }

    public static ASTArchSimpleArithmeticExpression createArithmeticExpression(MathNumberExpressionSymbol symbol) {
        ASTArchSimpleArithmeticExpressionBuilder builder = CNNArchMill.archSimpleArithmeticExpressionBuilder();
        builder.setSymbol(symbol);
        ASTNumberExpression numberExpression = createNumberExpression(symbol);
        builder.setNumberExpression(numberExpression);
        ASTArchSimpleArithmeticExpression ast = builder.build();
        symbol.setAstNode(ast);
        return ast;
    }

    public static ASTNumberExpression createNumberExpression(MathNumberExpressionSymbol symbol) {
        ASTNumberExpressionBuilder builder = MathMill.numberExpressionBuilder();
        builder.setSymbol(symbol);
        ASTNumberWithUnit numberWithUnit = createNumberWithUnit(symbol);
        builder.setNumberWithUnit(numberWithUnit);
        ASTNumberExpression ast = builder.build();
        symbol.setAstNode(ast);
        return ast;
    }

    public static ASTNumberWithUnit createNumberWithUnit(MathNumberExpressionSymbol symbol) {
        ASTNumberWithUnitBuilder builder = MathMill.numberWithUnitBuilder();
        ASTNumberWithInf numberWithInf = createNumberWithInf(symbol);
        builder.setNum(numberWithInf);
        return builder.build();
    }

    public static ASTNumberWithInf createNumberWithInf(MathNumberExpressionSymbol symbol) {
        ASTNumberWithInfBuilder builder = MathMill.numberWithInfBuilder();
        ASTIntLiteral intLiteral = createIntLiteral(symbol);
        builder.setNumber(intLiteral);
        return builder.build();
    }

    public static ASTIntLiteral createIntLiteral(MathNumberExpressionSymbol symbol) {
        ASTIntLiteralBuilder builder = MathMill.intLiteralBuilder();
        int value = RationalMath.getIntValue(symbol.getValue().getRealNumber());
        builder.setSource("" + value);
        return builder.build();
    }
}
