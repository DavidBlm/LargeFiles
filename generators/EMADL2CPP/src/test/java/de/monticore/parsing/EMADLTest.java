package de.monticore.parsing;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._ast.ASTEMACompilationUnit;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

class EMADLTest {

    @Test
    void exceptionByParsing() {
        assertThrows(IOException.class, () -> new EMADLParser().parseModelIfExists("wrong path"));
    }

    @Test
    void parsingEMA() throws IOException {
        final ASTEMACompilationUnit parsedModel = new EMADLParser().parseModelIfExists(
                "src/test/resources/models/pipeline/LinearPipeline.ema");
        assertEquals("LinearPipeline", parsedModel.getComponent().getName());
    }

    @Test
    void parsingEMADL() throws IOException {
        final ASTEMACompilationUnit parsedModel = new EMADLParser().parseModelIfExists(
                "src/test/resources/models/Add.emadl");
        assertEquals("Add", parsedModel.getComponent().getName());
    }
}