package de.monticore.parsing;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._ast.ASTEMACompilationUnit;

import java.io.IOException;
import java.util.Optional;


public class EMADLParser extends AbstractParser<ASTEMACompilationUnit>{

    public Optional<ASTEMACompilationUnit> parseModel(final String pathToModel) throws IOException {
        final de.monticore.lang.monticar.emadl._parser.EMADLParser embeddedMontiArcParser = new de.monticore.lang.monticar.emadl._parser.EMADLParser();
        return embeddedMontiArcParser.parse(pathToModel);
    }
}
