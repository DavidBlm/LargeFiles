/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.monticar.generator.FileContent;
import de.monticore.lang.monticar.generator.cpp.converter.ComponentConverter;
import de.monticore.lang.monticar.generator.middleware.helpers.FileHelper;
import de.monticore.lang.monticar.generator.middleware.helpers.NameHelper;
import de.monticore.lang.monticar.generator.middleware.helpers.TemplateHelper;
import de.monticore.lang.monticar.generator.middleware.impls.GeneratorImpl;
import de.monticore.lang.tagging._symboltable.TaggingResolver;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class MiddlewareGenerator extends CMakeGenerator {

    @Override
    public List<File> generate(EMAComponentInstanceSymbol componentInstanceSymbol, TaggingResolver taggingResolver) throws IOException {
        //Add dummy GeneratorImpl for the subdir
        String subdir = "coordinator/";
        this.add(new GeneratorImpl() {
        }, subdir);
        List<File> files = super.generate(componentInstanceSymbol, taggingResolver);

        files.add(FileHelper.generateFile(generationTargetPath + subdir, generateIAdapterHeader(componentInstanceSymbol)));
        files.add(FileHelper.generateFile(generationTargetPath + subdir, generateIAdapterCpp(componentInstanceSymbol)));
        files.add(FileHelper.generateFile(generationTargetPath + subdir, generateCoordinator(componentInstanceSymbol, files)));
        files.add(FileHelper.generateFile(generationTargetPath + subdir, generateCoordinatorCMakeList(componentInstanceSymbol, files)));
        return files;
    }

    private FileContent generateIAdapterCpp(EMAComponentInstanceSymbol componentInstanceSymbol) {
        FileContent res = new FileContent();
        String name = NameHelper.getNameTargetLanguage(componentInstanceSymbol.getFullName());
        res.setFileName("IAdapter_" + name + ".cpp");
        res.setFileContent("#include \"IAdapter_" + name + ".h\"");
        return res;
    }

    private FileContent generateIAdapterHeader(EMAComponentInstanceSymbol componentInstanceSymbol) {
        FileContent res = new FileContent();
        String name = NameHelper.getNameTargetLanguage(componentInstanceSymbol.getFullName());
        res.setFileName("IAdapter_" + name + ".h");
        res.setFileContent(TemplateHelper.getIAdapterTemplate().replace("${compName}", name));
        return res;
    }


    private FileContent generateCoordinator(EMAComponentInstanceSymbol componentInstanceSymbol, List<File> files) {


        String name = NameHelper.getNameTargetLanguage(componentInstanceSymbol.getFullName());
        List<String> filesNames = files.stream()
                .map(File::getName)
                .collect(Collectors.toList());

        String includes = filesNames.stream()
                .filter(fn -> fn.matches("(\\w+(Adapter_))?(" + name + "\\.h)"))
                .filter(fn -> !fn.startsWith("IAdapter"))
                .map(fn -> "#include \"" + fn + "\"")
                .sorted()
                .collect(Collectors.joining("\n"));

        String addAdapters = filesNames.stream()
                .filter(fn -> fn.matches("\\w+Adapter_" + name + "\\.h"))
                .filter(fn -> !fn.startsWith("IAdapter"))
                .map(fn -> fn.substring(0, fn.length() - 2))
                .map(fn -> "  adapters.push_back(new " + fn + "());")
                .sorted()
                .collect(Collectors.joining("\n"));

        String initParams = componentInstanceSymbol.getArguments().stream()
                .map(ComponentConverter::getExpressionParameterConversion)
                .collect(Collectors.joining(","));

        String content = TemplateHelper.getCoordinatorTemplate()
                .replace("${compName}", name)
                .replace("${includes}", includes)
                .replace("${addAdapters}", addAdapters)
                .replace("${initParams}",initParams);


        FileContent res = new FileContent();
        res.setFileName("Coordinator_" + name + ".cpp");
        res.setFileContent(content);
        return res;
    }


    private FileContent generateCoordinatorCMakeList(EMAComponentInstanceSymbol componentInstanceSymbol, List<File> files) {
        FileContent res = new FileContent();
        String name = NameHelper.getNameTargetLanguage(componentInstanceSymbol.getFullName());

        String targets = files.stream()
                .map(File::getName)
                .filter(fn -> fn.matches("(\\w+(Adapter_))?(" + name + "\\.h)"))
                .filter(fn -> !fn.startsWith("IAdapter"))
                .map(fn -> fn.substring(0, fn.length() - 2))
                .sorted()
                .collect(Collectors.joining(" "));

        String content = TemplateHelper.getCoordinatorCmakeListsTemplate()
                .replace("${targets}", targets)
                .replace("${compName}", name);

        res.setFileName("CMakeLists.txt");
        res.setFileContent(content);
        return res;
    }

}
