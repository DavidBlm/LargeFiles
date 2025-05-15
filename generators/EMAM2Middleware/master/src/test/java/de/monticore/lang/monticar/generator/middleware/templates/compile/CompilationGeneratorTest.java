/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.templates.compile;

import de.monticore.lang.monticar.generator.FileContent;
import de.monticore.lang.monticar.generator.middleware.AbstractSymtabTest;
import de.monticore.lang.monticar.generator.middleware.helpers.FileHelper;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class CompilationGeneratorTest extends AbstractSymtabTest {

    private static String OUT_BASE = "target/compileScripts/";

    @Test
    public void testRos() throws IOException {
        BashCompilationGenerator bashCompilationGenerator = new BashCompilationGenerator();
        bashCompilationGenerator.setUseRos(true);
        FileContent script = bashCompilationGenerator.getCompilationScripts().get(0);
        assertEquals("compile.sh", script.getFileName());
        File f1 = FileHelper.generateFile(OUT_BASE + "Ros/", script);
        testFilesAreEqual(Arrays.asList(f1), "compileScripts/Ros/");
    }

    @Test
    public void testNoRos() throws IOException {
        BashCompilationGenerator bashCompilationGenerator = new BashCompilationGenerator();
        FileContent script1 = bashCompilationGenerator.getCompilationScripts().get(0);
        assertEquals("compile.sh", script1.getFileName());
        File f1 = FileHelper.generateFile(OUT_BASE + "NoRos/", script1);

        MingwCompilationGenerator mingwCompilationGenerator = new MingwCompilationGenerator();
        FileContent script2 = mingwCompilationGenerator.getCompilationScripts().get(0);
        assertEquals("compileMingw.bat", script2.getFileName());
        File f2 = FileHelper.generateFile(OUT_BASE + "NoRos/", script2);

        MsbuildCompilationGenerator msbuildCompilationGenerator = new MsbuildCompilationGenerator();
        FileContent script3 = msbuildCompilationGenerator.getCompilationScripts().get(0);
        assertEquals("compileMsbuild.bat", script3.getFileName());
        File f3 = FileHelper.generateFile(OUT_BASE + "NoRos/", script3);

        testFilesAreEqual(Arrays.asList(f1, f2, f3), "compileScripts/NoRos/");
    }

    @Test
    public void testRos2() throws IOException {
        BashCompilationGenerator bashCompilationGenerator = new BashCompilationGenerator();
        bashCompilationGenerator.setUseRos2(true);
        FileContent script1 = bashCompilationGenerator.getCompilationScripts().get(0);
        assertEquals("compile.sh", script1.getFileName());
        File f1 = FileHelper.generateFile(OUT_BASE + "Ros2/", script1);

        MsbuildCompilationGenerator msbuildCompilationGenerator = new MsbuildCompilationGenerator();
        msbuildCompilationGenerator.setUseRos2(true);
        FileContent script3 = msbuildCompilationGenerator.getCompilationScripts().get(0);
        assertEquals("compileMsbuild.bat", script3.getFileName());
        File f2 = FileHelper.generateFile(OUT_BASE + "Ros2/", script3);

        testFilesAreEqual(Arrays.asList(f1, f2), "compileScripts/Ros2/");
    }
}
