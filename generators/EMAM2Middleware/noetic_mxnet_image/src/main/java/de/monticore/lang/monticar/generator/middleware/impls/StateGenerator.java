/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.impls;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.tagging._symboltable.TaggingResolver;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class StateGenerator implements GeneratorImpl{
    private GeneratorImpl delegateGenerator;
    private EMAComponentInstanceSymbol component;
    private TaggingResolver taggingResolver;

    public StateGenerator(GeneratorImpl delegateGenerator, EMAComponentInstanceSymbol component, TaggingResolver taggingResolver) {
        this.delegateGenerator = delegateGenerator;
        this.component = component;
        this.taggingResolver = taggingResolver;
    }

    public void setGenerationTargetPath(String path) {
        delegateGenerator.setGenerationTargetPath(path);
    }

    public List<File> generate() throws IOException {
        return delegateGenerator.generate(component, taggingResolver);
    }

    @Override
    public List<File> generate(EMAComponentInstanceSymbol componentInstanceSymbol, TaggingResolver taggingResolver) throws IOException {
        return generate();
    }
}
