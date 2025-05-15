package de.monticore.mlpipelines.automl.helper;

import conflang._ast.ASTConfLangCompilationUnit;
import conflang._ast.ASTConfiguration;
import conflang._symboltable.ConfLangLanguage;
import conflang._symboltable.ConfLangSymbolTableCreator;
import conflang._symboltable.ConfigurationScope;
import de.monticore.io.paths.ModelPath;
import de.monticore.symboltable.GlobalScope;
import de.monticore.symboltable.ResolvingConfiguration;
import de.se_rwth.commons.logging.Log;
import schemalang.validation.SchemaLangValidator;
import schemalang.validation.Violation;
import schemalang.validation.exception.SchemaLangException;

import java.nio.file.Paths;
import java.util.Collection;

public class ConfigurationValidationHandler {

    private static ConfigurationScope createConfLangSymbolTable(ASTConfiguration confLangConfiguration) {
        final ModelPath modelPath = new ModelPath(Paths.get(""));
        final ConfLangLanguage confLangLanguage = new ConfLangLanguage();
        final ResolvingConfiguration resolverConfiguration = new ResolvingConfiguration();
        resolverConfiguration.addDefaultFilters(confLangLanguage.getResolvingFilters());

        ConfLangSymbolTableCreator symbolTableCreator = new ConfLangSymbolTableCreator(resolverConfiguration, new GlobalScope(modelPath, confLangLanguage));
        return (ConfigurationScope) symbolTableCreator.createFromAST(confLangConfiguration);
    }

    private static String getScmName(ASTConfiguration configuration) {
        String scmName = configuration.getName();
        if (scmName.startsWith("mnistClassifier")) {
            scmName = "Supervised";
        } else if (scmName.equals("AdaNet")) {
            scmName = "NeuralArchitectureSearch";
        }
        return scmName;
    }

    public static void validateConfiguration(ASTConfLangCompilationUnit compilationUnit, String schemaPath) {
        if (compilationUnit == null) {
            return;
        }
        ASTConfiguration configuration = compilationUnit.getConfiguration();
        String scmName = getScmName(configuration);
        Log.info(String.format("Validate %s configuration using schema.", scmName), ConfigurationValidationHandler.class.getName());
        createConfLangSymbolTable(configuration);
        ModelPath schemaModelPath = new ModelPath(Paths.get(schemaPath));
        try {
            Collection<Violation> violations = SchemaLangValidator.validate(configuration.getConfigurationSymbol(),
                    scmName, null, schemaModelPath);
            if (!violations.isEmpty()) {
                String errorMsg = "";
                for (Violation violation : violations) {
                    errorMsg += "\n" + violation.toString();
                }
                throw new SchemaLangException(errorMsg);
            }
        } catch (SchemaLangException e) {
            throw new RuntimeException(e);
        }
    }
}
