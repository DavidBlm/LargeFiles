package de.monticore.mlpipelines.automl.trainalgorithms.adanet.custom.models;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.SerialCompositeElementSymbol;

import java.util.List;

public class SerialCompositeElementSymbolCustom extends SerialCompositeElementSymbol {
    public SerialCompositeElementSymbolCustom() {
        super();
    }

    @Override
    public void setElements(List<ArchitectureElementSymbol> elements) {
        super.setElements(elements);
    }
}
