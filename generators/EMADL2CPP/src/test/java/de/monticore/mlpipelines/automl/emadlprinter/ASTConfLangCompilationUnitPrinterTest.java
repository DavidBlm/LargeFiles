package de.monticore.mlpipelines.automl.emadlprinter;

import conflang._ast.ASTConfLangCompilationUnit;
import conflang._parser.ConfLangParser;
import de.monticore.mlpipelines.automl.helper.ASTConfLangCompilationUnitHandler;
import de.monticore.mlpipelines.automl.helper.FileLoader;
import junit.framework.TestCase;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class ASTConfLangCompilationUnitPrinterTest extends TestCase {

    private ASTConfLangCompilationUnit getCompilationUnit(String modelPath, String model) {
        ConfLangParser parser = new ConfLangParser();
        Path path = Paths.get(modelPath, model);
        ASTConfLangCompilationUnit compilationUnit = null;
        try {
            compilationUnit = parser.parse(path.toString()).get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return compilationUnit;
    }

    public void testPrettyPrintNetworkConf() {
        this.prettyPrinterGeneralTest("Network.conf");
    }

    public void testPrettyPrintAdaptedNetworkConf() {
        this.prettyPrinterGeneralTest("Network.conf", "num_epoch", 100);
    }

    public void testPrettyPrintEfficientNetConf() {
        this.prettyPrinterGeneralTest("EfficientNet.conf");
    }

    public void testPrettyPrintEvaluationCriteriaConf() {
        this.prettyPrinterGeneralTest("EvaluationCriteria.conf");
    }

    public void testPrettyPrintHyperparameterOptConf() {
        this.prettyPrinterGeneralTest("HyperparameterOpt.conf");
    }

    private void prettyPrinterGeneralTest (String model, String key, Object value) {
        String modelPath = "src/test/resources/models/automl";
        ASTConfLangCompilationUnit compilationUnit = this.getCompilationUnit(modelPath, model);

        String expectedModel = model;

        if ((key != null) && (value != null)) {
            ASTConfLangCompilationUnitHandler.setValueForKey(compilationUnit, key, value);
            expectedModel = "adapted/" + expectedModel;
        }

        ASTConfLangCompilationUnitPrinter printer = new ASTConfLangCompilationUnitPrinter();
        String[] conf = printer.prettyPrint(compilationUnit).split("\n");

        List<String> expectedConf = FileLoader.loadFile("src/test/resources/models/automl/" + expectedModel);
        assertEquals(expectedConf.size(), conf.length);
        for (int i = 0; i < conf.length; i++) {
            System.out.println(conf[i]);
            assertEquals(expectedConf.get(i), conf[i]);
        }
    }

    private void prettyPrinterGeneralTest (String model) {
        this.prettyPrinterGeneralTest(model, null, null);
    }
}
