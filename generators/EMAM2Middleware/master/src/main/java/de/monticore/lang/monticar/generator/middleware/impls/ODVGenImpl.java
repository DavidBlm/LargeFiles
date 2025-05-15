/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.impls;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.se_rwth.commons.logging.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ODVGenImpl implements  GeneratorImpl {

    private String generationTargetPath;

    @Override
    public List<File> generate(EMAComponentInstanceSymbol componentInstanceSymbol, TaggingResolver taggingResolver) throws IOException {
        //TODO: implement ODV generator in new Project(e.g. EMAM2ODV)
        Log.warn("ODV is not yet implemented!");
        return new ArrayList<>();
    }

    @Override
    public void setGenerationTargetPath(String path) {
        this.generationTargetPath = path;
    }

    @Override
    public boolean willAccept(EMAComponentInstanceSymbol componentInstanceSymbol) {
        //TODO: check if component has ODV Ports
        return true;
    }
}
