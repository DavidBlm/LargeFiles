/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.impls;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAPortInstanceSymbol;
import de.monticore.lang.monticar.generator.roscpp.GeneratorRosCpp;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.se_rwth.commons.logging.Log;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RclCppGenImpl implements GeneratorImpl {
    private String generationTargetPath;
    private GeneratorRosCpp generatorRosCpp;

    public RclCppGenImpl(){
        generatorRosCpp = new GeneratorRosCpp();
        generatorRosCpp.setGenerateCMake(true);
        generatorRosCpp.setRos2Mode(true);
    }

    public void setGeneratorRosCpp(GeneratorRosCpp generatorRosCpp) {
        this.generatorRosCpp = generatorRosCpp;
    }

    @Override
    public List<File> generate(EMAComponentInstanceSymbol componentInstanceSymbol, TaggingResolver taggingResolver) throws IOException {
        generatorRosCpp.setGenerationTargetPath(generationTargetPath);
        return generatorRosCpp.generateFiles(componentInstanceSymbol, taggingResolver);
    }

    @Override
    public void setGenerationTargetPath(String path) {
        this.generationTargetPath = path;
    }

    @Override
    public boolean willAccept(EMAComponentInstanceSymbol componentInstanceSymbol) {
        boolean result = componentInstanceSymbol.getPortInstanceList().stream().anyMatch(EMAPortInstanceSymbol::isRosPort);
        if(!result){
            Log.warn("Generator rclcpp: No ROS Ports found! Ignoring component " + componentInstanceSymbol.getName());
        }

        return result;
    }
}
