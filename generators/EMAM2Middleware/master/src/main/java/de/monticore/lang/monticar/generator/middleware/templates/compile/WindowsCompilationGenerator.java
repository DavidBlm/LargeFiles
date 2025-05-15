/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.templates.compile;

import de.monticore.lang.monticar.generator.FileContent;
import de.monticore.lang.monticar.generator.middleware.templates.MiddlewareTemplates;

import java.util.ArrayList;
import java.util.List;

public abstract class WindowsCompilationGenerator extends CompilationGenerator {
    @Override
    public String getNewlineDelimiter() {
        return "\r\n";
    }

    @Override
    public List<FileContent> getCompilationScripts() {
        ArrayList<FileContent> fileContents = new ArrayList<>();
        fileContents.addAll(super.getCompilationScripts());

        FileContent substScript = new FileContent();
        substScript.setFileName("subst" + getFileName().substring(0,1).toUpperCase() + getFileName().substring(1));
        substScript.setFileContent(MiddlewareTemplates.generateCompileSubst(this));
        fileContents.add(substScript);

        return fileContents;
    }

    abstract WinGenKind getKind();

    enum WinGenKind{
        MINGW,
        MSBUILD
    }
}
