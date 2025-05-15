package de.monticore.mlpipelines.automl.trainalgorithms.adanet.custom.models;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchExpressionSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArgumentSymbol;

public class ArgumentSymbolCustom extends ArgumentSymbol {
    public ArgumentSymbolCustom(String name) {
        super(name);
    }

    public void setRhs(ArchExpressionSymbol rhs) {
        super.setRhs(rhs);
    }
}
