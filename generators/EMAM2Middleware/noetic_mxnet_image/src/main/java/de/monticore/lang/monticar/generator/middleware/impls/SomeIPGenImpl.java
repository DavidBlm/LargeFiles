/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.impls;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAPortInstanceSymbol;
import de.monticore.lang.monticar.generator.someip.GeneratorSomeIP;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.se_rwth.commons.logging.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SomeIPGenImpl implements GeneratorImpl {
    private String generationTargetPath;
    private GeneratorSomeIP generatorSomeIP;

    public SomeIPGenImpl(){
        generatorSomeIP = new GeneratorSomeIP();
    }

    public void setGeneratorSomeIP(GeneratorSomeIP generatorSomeIP) {
        this.generatorSomeIP = generatorSomeIP;
    }

    @Override
    public List<File> generate(EMAComponentInstanceSymbol componentInstanceSymbol, TaggingResolver taggingResolver) throws IOException {
        generatorSomeIP.setGenerationTargetPath(generationTargetPath);
        return generatorSomeIP.generateSomeIPAdapter(componentInstanceSymbol);
    }

    @Override
    public void setGenerationTargetPath(String path) {
        this.generationTargetPath = path;
    }

    @Override
    public boolean willAccept(EMAComponentInstanceSymbol componentInstanceSymbol) {
        boolean result = componentInstanceSymbol.getPortInstanceList().stream().anyMatch(EMAPortInstanceSymbol::isSomeIPPort);
        if(!result){
            Log.warn("GeneratorSomeIP: No SomeIP Ports found! Ignoring component " + componentInstanceSymbol.getName());
        }

        return result;
    }
}
