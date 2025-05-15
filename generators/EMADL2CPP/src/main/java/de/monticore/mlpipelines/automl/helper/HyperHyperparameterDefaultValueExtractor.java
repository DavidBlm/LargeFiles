package de.monticore.mlpipelines.automl.helper;

import de.monticore.mcliterals._ast.ASTSignedDoubleLiteral;
import de.monticore.mcliterals._ast.ASTSignedIntLiteral;
import de.monticore.mcliterals._ast.ASTSignedLiteral;
import schemalang._ast.*;
import schemalang._parser.SchemaLangParser;

import java.io.IOException;
import java.util.List;

public class HyperHyperparameterDefaultValueExtractor {

    public static Object getDefaultValue(String schemaPath, String algName, String hyperHyperparamsName) {
        ASTSchemaLangCompilationUnit schemaLangCompilationUnit = getSchemaLangCompilationUnit(schemaPath);
        ASTSchemaMember optimizerMember = schemaLangCompilationUnit.getSchemaDefinition().getSchemaMember(0);
        ASTSignedLiteral hyperHyperparameterLiteral = getLiteral(optimizerMember, algName, hyperHyperparamsName);
        return getLiteralValue(hyperHyperparameterLiteral);
    }

    private static ASTSchemaLangCompilationUnit getSchemaLangCompilationUnit(String schemaPath) {
        ASTSchemaLangCompilationUnit schemaLangCompilationUnit;
        try {
            schemaLangCompilationUnit = new SchemaLangParser().parse(schemaPath).get();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return schemaLangCompilationUnit;
    }

    private static ASTSignedLiteral getLiteral(ASTSchemaMember optimizerMember,
                                                   String algName, String hyperHyperparameterName) {
        List<ASTComplexPropertyValueDefinition> definitionList = ((ASTComplexPropertyDefinition) optimizerMember).getComplexPropertyValueDefinitionList();
        ASTComplexPropertyValueDefinition algDefinition = definitionList.stream().filter(
                definition -> algName.equals(definition.getName())).findFirst().orElse(null);
        List<ASTSchemaMember> schemaMemberList = algDefinition.getSchemaMemberList();
        ASTSchemaMember hyperHyperparameterMember = schemaMemberList.stream().filter(
                member -> hyperHyperparameterName.equals(member.getName())).findFirst().orElse(null);
        return ((ASTTypedDeclaration) hyperHyperparameterMember).getInitial();
    }

    private static Object getLiteralValue(ASTSignedLiteral hyperHyperparameterLiteral) {
        Object defaultValue;
        if (hyperHyperparameterLiteral instanceof ASTSignedDoubleLiteral) {
            defaultValue = ((ASTSignedDoubleLiteral) hyperHyperparameterLiteral).getValue();
        } else {
            defaultValue = ((ASTSignedIntLiteral) hyperHyperparameterLiteral).getValue();
        }
        return defaultValue;
    }
}
