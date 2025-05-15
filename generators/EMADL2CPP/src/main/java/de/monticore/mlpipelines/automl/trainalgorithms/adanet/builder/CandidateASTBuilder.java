package de.monticore.mlpipelines.automl.trainalgorithms.adanet.builder;

import de.monticore.lang.monticar.cnnarch._ast.ASTParallelBlock;
import de.monticore.lang.monticar.cnnarch._symboltable.ParallelCompositeElementSymbol;
import de.monticore.mlpipelines.automl.trainalgorithms.ASTGenerator;

public class CandidateASTBuilder {
    ParallelCompositeElementSymbol adanetSymbol;

    public ASTParallelBlock build(ParallelCompositeElementSymbol adanetSymbol) {
        this.adanetSymbol = adanetSymbol;
        return buildParallelBlock();
    }

    private ASTParallelBlock buildParallelBlock() {
        return ASTGenerator.createParallelBlockForSymbol(adanetSymbol);
    }
}