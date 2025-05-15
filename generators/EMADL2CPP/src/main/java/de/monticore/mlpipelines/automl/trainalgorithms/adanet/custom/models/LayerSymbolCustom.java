package de.monticore.mlpipelines.automl.trainalgorithms.adanet.custom.models;

import de.monticore.lang.monticar.cnnarch._symboltable.ArgumentSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.LayerSymbol;

import java.util.List;

public class LayerSymbolCustom extends LayerSymbol {
    public LayerSymbolCustom(String name) {
        super(name);
    }

    public List<ArgumentSymbol> getArguments() {
        return super.getArguments();
    }

    public void setArguments(List<ArgumentSymbol> arguments) {
        super.setArguments(arguments);
    }
}
