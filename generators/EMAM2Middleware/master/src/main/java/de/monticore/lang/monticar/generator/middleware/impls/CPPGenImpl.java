/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.impls;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.generator.cpp.GeneratorCPP;
import de.monticore.lang.tagging._symboltable.TaggingResolver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CPPGenImpl implements GeneratorImpl {
    private String generationTargetPath;
    private GeneratorCPP generatorCPP;
    private String modelsDir;
    private boolean executionLoggingActive = false;

    public CPPGenImpl(String modelsDir){
        this.modelsDir = modelsDir;
    }

    public void setExecutionLoggingActive(boolean active){
        executionLoggingActive = active;
    }

    private void resetGenerator(){
        generatorCPP = new GeneratorCPP();
        generatorCPP.useArmadilloBackend();
        generatorCPP.setGenerateCMake(true);
        generatorCPP.setModelsDirPath(Paths.get(modelsDir));
        generatorCPP.setExecutionLoggingActive(executionLoggingActive);
        //generatorCPP.setGenerateTests(true);
    }

    public void setGeneratorCPP(GeneratorCPP generatorCPP){
        this.generatorCPP = generatorCPP;
    }

    @Override
    public List<File> generate(EMAComponentInstanceSymbol componentInstanceSymbol, TaggingResolver taggingResolver) throws IOException {
        resetGenerator();
        List<File> files = new ArrayList<>();

        generatorCPP.setGenerationTargetPath(generationTargetPath);
        files.addAll(generatorCPP.generateFiles(taggingResolver, componentInstanceSymbol));

        return files;
    }

    @Override
    public void setGenerationTargetPath(String path) {
        this.generationTargetPath = path;
    }

}
