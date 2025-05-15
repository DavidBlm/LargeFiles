package de.monticore.mlpipelines.automl.trainalgorithms.adanet.builder;

import de.monticore.lang.math._ast.ASTNumberExpression;
import de.monticore.lang.monticar.cnnarch._ast.*;
import de.monticore.lang.monticar.cnnarch._symboltable.ParallelCompositeElementSymbol;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetCandidate;
import de.monticore.mlpipelines.automl.trainalgorithms.adanet.models.AdaNetComponent;
import de.monticore.numberunit._ast.ASTNumberWithUnit;
import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class CandidateASTBuilderTest extends TestCase {

    @Test
    public void testConstructor() {
        CandidateASTBuilder candidateASTBuilder = new CandidateASTBuilder();
        assertNotNull(candidateASTBuilder);
    }

    @Test
    public void testBuildOutputsNotNull() {
        CandidateASTBuilder candidateASTBuilder = new CandidateASTBuilder();
        ParallelCompositeElementSymbol adanetSymbol = mock(ParallelCompositeElementSymbol.class);
        ASTParallelBlock adanetAst = candidateASTBuilder.build(adanetSymbol);
        assertNotNull(adanetAst);
    }

    @Test
    public void testBuildCreatesAst() {
        CandidateASTBuilder candidateASTBuilder = new CandidateASTBuilder();
        AdaNetCandidate candidate = new AdaNetCandidate(new AdaNetComponent(3), new ArrayList<>());
        CandidateSymbolBuilder candidateSymbolBuilder = new CandidateSymbolBuilder();
        ParallelCompositeElementSymbol adanetSymbol = candidateSymbolBuilder.build(candidate);

        ASTParallelBlock adanetAst = candidateASTBuilder.build(adanetSymbol);
        assertNotNull(adanetAst);
    }

    @Test
    public void testBuildCreatesValidParallelBlock() {
        CandidateASTBuilder candidateASTBuilder = new CandidateASTBuilder();
        AdaNetCandidate candidate = new AdaNetCandidate(new AdaNetComponent(3), new ArrayList<>());
        CandidateSymbolBuilder candidateSymbolBuilder = new CandidateSymbolBuilder();
        ParallelCompositeElementSymbol adanetSymbol = candidateSymbolBuilder.build(candidate);

        ASTParallelBlock adanetAst = candidateASTBuilder.build(adanetSymbol);
        assertNotNull(adanetAst);
        assertTrue(adanetAst.getSymbol() instanceof ParallelCompositeElementSymbol);
        assertEquals(1, adanetAst.getGroupsList().size());
    }

    @Test
    public void testBuildCreatesValidASTStream() {
        CandidateASTBuilder candidateASTBuilder = new CandidateASTBuilder();
        AdaNetCandidate candidate = new AdaNetCandidate(new AdaNetComponent(3), new ArrayList<>());
        CandidateSymbolBuilder candidateSymbolBuilder = new CandidateSymbolBuilder();
        ParallelCompositeElementSymbol adanetSymbol = candidateSymbolBuilder.build(candidate);

        ASTParallelBlock adanetAst = candidateASTBuilder.build(adanetSymbol);
        ASTStream stream = adanetAst.getGroupsList().get(0);
        assertNotNull(stream);
        assertEquals(6, stream.getElementsList().size());
    }

    @Test
    public void testBuildCreatesValidLayer() {
        CandidateASTBuilder candidateASTBuilder = new CandidateASTBuilder();
        AdaNetCandidate candidate = new AdaNetCandidate(new AdaNetComponent(3), new ArrayList<>());
        CandidateSymbolBuilder candidateSymbolBuilder = new CandidateSymbolBuilder();
        ParallelCompositeElementSymbol adanetSymbol = candidateSymbolBuilder.build(candidate);

        ASTParallelBlock adanetAst = candidateASTBuilder.build(adanetSymbol);

        ASTStream stream = adanetAst.getGroupsList().get(0);
        ASTLayer layer = (ASTLayer) stream.getElementsList().get(1);
        assertNotNull(layer);
        assertEquals("Concatenate", layer.getName());
    }

    @Test
    public void testBuildCreatesValidArguments() {
        CandidateASTBuilder candidateASTBuilder = new CandidateASTBuilder();
        AdaNetCandidate candidate = new AdaNetCandidate(new AdaNetComponent(3), new ArrayList<>());
        CandidateSymbolBuilder candidateSymbolBuilder = new CandidateSymbolBuilder();
        ParallelCompositeElementSymbol adanetSymbol = candidateSymbolBuilder.build(candidate);

        ASTParallelBlock adanetAst = candidateASTBuilder.build(adanetSymbol);

        ASTStream stream = adanetAst.getGroupsList().get(0);
        ASTParallelBlock parallelLayer = (ASTParallelBlock) stream.getElementsList().get(0);
        ASTStream stream2 = parallelLayer.getGroups(0);
        ASTLayer layer = (ASTLayer) stream2.getElementsList().get(0);
        ASTArchParameterArgument argument = (ASTArchParameterArgument) layer.getArgumentsList().get(0);
        int number = (int) getValueOfArgument(argument);


        assertNotNull(layer);
        assertEquals("FullyConnected", layer.getName());
        assertEquals(1, layer.getArgumentsList().size());
        assertEquals("units", argument.getName());
        assertEquals(20, number);
    }

    private static double getValueOfArgument(ASTArchParameterArgument argument) {
        ASTArchExpression rhs = argument.getRhs();
        ASTArchSimpleExpression simpleExpression = rhs.getExpression();
        ASTArchSimpleArithmeticExpression arithmeticExpression = (ASTArchSimpleArithmeticExpression) simpleExpression.getArithmeticExpression();
        ASTNumberExpression numberExpression = arithmeticExpression.getNumberExpression();
        ASTNumberWithUnit numberWithUnit = numberExpression.getNumberWithUnit();
        return numberWithUnit.getNumber().get();
    }

    @Test
    public void testAstParallelBlockHasValidArguments() {
        CandidateASTBuilder candidateASTBuilder = new CandidateASTBuilder();
        AdaNetCandidate candidate = new AdaNetCandidate(new AdaNetComponent(3), new ArrayList<>());
        CandidateSymbolBuilder candidateSymbolBuilder = new CandidateSymbolBuilder();
        ParallelCompositeElementSymbol adanetSymbol = candidateSymbolBuilder.build(candidate);
        ASTParallelBlock adanetAst = candidateASTBuilder.build(adanetSymbol);

    }
}