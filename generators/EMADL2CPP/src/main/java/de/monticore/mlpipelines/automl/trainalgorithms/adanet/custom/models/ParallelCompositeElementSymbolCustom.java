package de.monticore.mlpipelines.automl.trainalgorithms.adanet.custom.models;

import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureElementSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ParallelCompositeElementSymbol;

import java.util.List;

public class ParallelCompositeElementSymbolCustom extends ParallelCompositeElementSymbol {
    @Override
    public void setElements(List<ArchitectureElementSymbol> elements) {
        super.setElements(elements);
    }
}
