package de.monticore.mlpipelines.validation;

import conflang._symboltable.ConfigurationSymbol;
import de.monticore.io.paths.ModelPath;
import de.monticore.lang.monticar.cnnarch.generator.util.SchemaUtil;
import schemalang._symboltable.SchemaDefinitionSymbol;
import schemalang.validation.SchemaLangValidator;
import schemalang.validation.SchemaViolation;
import schemalang.validation.exception.SchemaLangException;
import schemalang.validation.exception.SchemaLangValidationException;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static de.monticore.lang.monticar.cnnarch.generator.validation.Constants.ROOT_SCHEMA;
import static de.monticore.lang.monticar.cnnarch.generator.validation.Constants.ROOT_SCHEMA_MODEL_PATH;

public class ConfigurationValidator {

    public void validateTrainingConfiguration(final ConfigurationSymbol trainingConfiguration) {
        try {
            Path schemasPath = Paths.get("target/classes",
                    ROOT_SCHEMA_MODEL_PATH);//fileSystem.getPath(schemasResource.getPath());
            ModelPath schemasModelPath = new ModelPath(schemasPath);
            final SchemaDefinitionSymbol schemaDefinition = SchemaUtil.resolveSchemaDefinition(ROOT_SCHEMA,
                    schemasModelPath);
            final List<SchemaDefinitionSymbol> schemaDefinitionSymbols = SchemaLangValidator.resolveSchemaHierarchy(
                    schemaDefinition, trainingConfiguration, schemasModelPath);
            List<SchemaViolation> schemaViolations = SchemaLangValidator.validateConfiguration(schemaDefinitionSymbols,
                    trainingConfiguration);
            if (schemaViolations.size() > 0)
                throw new SchemaLangValidationException();
        } catch (SchemaLangException e) {
            throw new RuntimeException(e);
        }
    }

    private FileSystem initFileSystem(URI uri) throws IOException {
        try {
            FileSystem aDefault = FileSystems.getDefault();
            return aDefault;
        } catch (Exception e) {
            Map<String, String> env = new HashMap<>();
            env.put("create", "true");
            FileSystem fileSystem = FileSystems.newFileSystem(uri, env);
            return fileSystem;
        }
    }
}
