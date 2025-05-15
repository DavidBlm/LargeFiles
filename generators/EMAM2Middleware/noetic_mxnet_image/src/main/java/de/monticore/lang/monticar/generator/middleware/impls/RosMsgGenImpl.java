/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.impls;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAPortInstanceSymbol;
import de.monticore.lang.monticar.generator.rosmsg.GeneratorRosMsg;
import de.monticore.lang.monticar.struct._symboltable.StructSymbol;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.monticore.symboltable.references.SymbolReference;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Stream;

public class RosMsgGenImpl implements GeneratorImpl {

    private String generationTargetPath;
    private boolean ros2mode = false;

    public RosMsgGenImpl(boolean ros2mode) {
        this.ros2mode = ros2mode;
    }

    @Override
    public List<File> generate(EMAComponentInstanceSymbol componentInstanceSymbol, TaggingResolver taggingResolver) throws IOException {
        GeneratorRosMsg generatorRosMsg = new GeneratorRosMsg();
        generatorRosMsg.setTarget(generationTargetPath, "struct_msgs");
        generatorRosMsg.setRos2mode(ros2mode);
        return generatorRosMsg.generateProject(componentInstanceSymbol);
    }

    @Override
    public void setGenerationTargetPath(String path) {
        this.generationTargetPath = path;
    }

    @Override
    public boolean willAccept(EMAComponentInstanceSymbol componentInstanceSymbol) {
        Stream<EMAPortInstanceSymbol> p = componentInstanceSymbol.getPortInstanceList().stream();
        Stream<EMAPortInstanceSymbol> subp = componentInstanceSymbol.getSubComponents().stream().flatMap(sc -> sc.getPortInstanceList().stream());
        Stream<EMAPortInstanceSymbol> relevantPorts = Stream.concat(p, subp);

        return relevantPorts
                .map(EMAPortInstanceSymbol::getTypeReference)
                .filter(SymbolReference::existsReferencedSymbol)
                .anyMatch(mcTypeReference -> mcTypeReference.getReferencedSymbol() instanceof StructSymbol);
    }
}
