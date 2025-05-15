package de.monticore.parsing;

import schemalang._ast.ASTSchemaLangCompilationUnit;
import schemalang._parser.SchemaLangParser;

import java.io.IOException;
import java.util.Optional;

@SuppressWarnings("unused")
public class SchemaLanguageParser extends AbstractParser<ASTSchemaLangCompilationUnit> {
    @Override
    public Optional<ASTSchemaLangCompilationUnit> parseModel(final String pathToModel) throws IOException {
        final SchemaLangParser schemaLangParser = new SchemaLangParser();
        return schemaLangParser.parse(pathToModel);
    }
}
