package de.monticore.parsing;

import conflang._ast.ASTConfLangCompilationUnit;

import java.io.IOException;
import java.util.Optional;

@SuppressWarnings("unused")
public class ConfigurationLanguageParser extends AbstractParser<ASTConfLangCompilationUnit> {

    public Optional<ASTConfLangCompilationUnit> parseModel(final String pathToModel) throws IOException {
        final conflang._parser.ConfLangParser confLangParser = new conflang._parser.ConfLangParser();
        return confLangParser.parse(pathToModel);
    }


}
