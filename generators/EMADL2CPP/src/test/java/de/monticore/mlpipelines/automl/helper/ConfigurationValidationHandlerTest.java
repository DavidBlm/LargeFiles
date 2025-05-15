package de.monticore.mlpipelines.automl.helper;

import conflang._ast.ASTConfLangCompilationUnit;
import conflang._parser.ConfLangParser;
import junit.framework.TestCase;

import java.io.IOException;

public class ConfigurationValidationHandlerTest extends TestCase {

    String schemaPath = "src/test/resources/models/automl/schemas/";

    public void testAdaNetValidation() throws IOException {
        ASTConfLangCompilationUnit adaNetConf = this.getConfiguration("AdaNet.conf");
        ConfigurationValidationHandler.validateConfiguration(adaNetConf, schemaPath);
    }

    public void testEfficientNetValidation() throws IOException {
        ASTConfLangCompilationUnit EfficientNetConf = this.getConfiguration("EfficientNet.conf");
        ConfigurationValidationHandler.validateConfiguration(EfficientNetConf, schemaPath);
    }

    public void testEvaluationCriteriaValidation() throws IOException {
        ASTConfLangCompilationUnit evaluationCriteriaConf = this.getConfiguration("EvaluationCriteria.conf");
        ConfigurationValidationHandler.validateConfiguration(evaluationCriteriaConf, schemaPath);
    }

    public void testTrainingConfigValidation() throws IOException {
        ASTConfLangCompilationUnit trainingConf = this.getConfiguration("mnistClassifier_net.conf");
        ConfigurationValidationHandler.validateConfiguration(trainingConf, schemaPath);
    }

    private ASTConfLangCompilationUnit getConfiguration(String confName) throws IOException {
        String path = "src/test/resources/models/automl/%s";
        path = String.format(path, confName);
        ConfLangParser parser = new ConfLangParser();
        ASTConfLangCompilationUnit compilationUnit = parser.parse(path).get();
        return compilationUnit;
    }
}
