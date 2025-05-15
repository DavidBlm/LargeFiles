/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.templates.compile;

import de.monticore.lang.monticar.generator.middleware.templates.MiddlewareTemplates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MingwCompilationGenerator extends WindowsCompilationGenerator {

    @Override
    public String getContent() {
        return MiddlewareTemplates.generateCompileMingw(this);
    }

    @Override
    public WinGenKind getKind() {
        return WinGenKind.MINGW;
    }

    @Override
    public boolean supportsRos() {
        return false;
    }

    @Override
    public boolean supportsRos2() {
        return false;
    }

    @Override
    public List<String> getAdditionalPathDirs() {
        return Arrays.asList("cmake","make","g++");
    }

    @Override
    public String getFileName() {
        return "compileMingw.bat";
    }

    @Override
    public List<String> getPostSourceExecutables() {
        return new ArrayList<>();
    }

    @Override
    public List<String> getEnvironmentFiles() {
        return new ArrayList<>();
    }

    @Override
    public List<String> getExecutables() {
        return Arrays.asList("cmake","make","g++");
    }
}
