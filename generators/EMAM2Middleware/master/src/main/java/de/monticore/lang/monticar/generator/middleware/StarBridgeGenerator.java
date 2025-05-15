/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.generator.middleware.impls.GeneratorImpl;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.se_rwth.commons.logging.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StarBridgeGenerator implements GeneratorImpl {
    private Map<GeneratorImpl, String> generatorImpls = new HashMap<>();
    private List<GeneratorImpl> generators = new ArrayList<>();
    String generationTargetPath;

    public void add(GeneratorImpl generator, String subdir) {
        generators.add(generator);
        generatorImpls.put(generator, subdir);
    }

    public void add(GeneratorImpl generator, String subdir, int i) {
        generators.add(i, generator);
        generatorImpls.put(generator, subdir);
    }

    public List<GeneratorImpl> getGeneratorImpls() {
        return generators;
    }

    public String getImplSubdir(GeneratorImpl generator) {
        return generatorImpls.get(generator);
    }

    public boolean remove(GeneratorImpl generator) {
        return generatorImpls.remove(generator) != null;
    }

    @Override
    public void setGenerationTargetPath(String path) {
        this.generationTargetPath = path.endsWith("/") ? path : path + "/";
    }

    public List<File> generate(EMAComponentInstanceSymbol componentInstanceSymbol, TaggingResolver taggingResolver) throws IOException {
        List<File> result = new ArrayList<>();
        generators.forEach(key -> {
            String value = generatorImpls.get(key);
            if (key.willAccept(componentInstanceSymbol)) {
                String fullTargetPath = generationTargetPath;
                if (value != null)
                    fullTargetPath = fullTargetPath + value;

                if (!fullTargetPath.endsWith("/"))
                    fullTargetPath = fullTargetPath + "/";

                key.setGenerationTargetPath(fullTargetPath);

                try {
                    result.addAll(key.generate(componentInstanceSymbol, taggingResolver));
                } catch (IOException e) {
                    Log.error("IOException occurred!", e);
                }
            }
        });

        return result;
    }


}
