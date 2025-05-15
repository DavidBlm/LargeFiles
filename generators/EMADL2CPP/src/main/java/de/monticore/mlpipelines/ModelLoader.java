package de.monticore.mlpipelines;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.symboltable.Scope;

import static de.monticore.lang.monticar.emadl.generator.EMADLAbstractSymtab.createSymTab;

public class ModelLoader {
    public static ArchitectureSymbol load(String modelFolderPath, String modelName){
        Scope symTab = createSymTab(modelFolderPath);
        EMAComponentInstanceSymbol c = symTab.<EMAComponentInstanceSymbol>resolve(modelName,
                        EMAComponentInstanceSymbol.KIND)
                .orElse(null);

        ArchitectureSymbol arch = c.getSpannedScope().<ArchitectureSymbol>resolve("", ArchitectureSymbol.KIND).get();
        return arch;
    }

    public static ArchitectureSymbol loadEfficientnetB0() {
        String modelFolderPath = "src/test/resources/models";
        String modelName = "efficientNetB0";

        return load(modelFolderPath, modelName);
    }

    public static ArchitectureSymbol loadAdaNetBase() {
        String modelFolderPath = "src/test/resources/models/adanet";
        String modelName = "adaNetBase";

        return load(modelFolderPath, modelName);
    }

    public static ArchitectureSymbol loadAdaNetStart() {
        String modelFolderPath = "src/test/resources/models/adanet";
        String modelName = "adaNetStart";

        return load(modelFolderPath, modelName);
    }
}