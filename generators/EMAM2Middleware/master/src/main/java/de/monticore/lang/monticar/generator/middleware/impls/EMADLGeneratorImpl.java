/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.impls;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.emadl.generator.Backend;
import de.monticore.lang.monticar.emadl.generator.EMADLGenerator;
import de.monticore.lang.monticar.generator.FileContent;

import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.se_rwth.commons.logging.Log;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class EMADLGeneratorImpl implements GeneratorImpl {
    private String generationTargetPath;
    private EMADLGenerator emadlGenerator;
    final String DEFAULT_BACKEND = "MXNET";


    public EMADLGeneratorImpl(String modelPath, String backendString){
        Optional<Backend> backend = Backend.getBackendFromString(backendString);
        if (!backend.isPresent()){
            Log.warn("specified backend " + backendString + " not supported. backend set to default value " + DEFAULT_BACKEND);
            backend = Backend.getBackendFromString(DEFAULT_BACKEND);
        }
        emadlGenerator = new EMADLGenerator(backend.get());
        emadlGenerator.setModelsPath(modelPath);
    }


    @Override
    public List<File> generate(EMAComponentInstanceSymbol componentInstanceSymbol, TaggingResolver taggingResolver) throws IOException {
        List<File> files = new ArrayList<>();

        emadlGenerator.setGenerationTargetPath(generationTargetPath);
        List<FileContent> fileContents = emadlGenerator.generateStrings(taggingResolver, componentInstanceSymbol, new HashSet<>(), "n");

        emadlGenerator.generateCMakeFiles(componentInstanceSymbol);

        for (FileContent fileContent : fileContents) {
            files.add(emadlGenerator.getEmamGen().generateFile(fileContent));
        }
        return files;
    }

    @Override
    public void setGenerationTargetPath(String path) {
        this.generationTargetPath = path;
    }

}

