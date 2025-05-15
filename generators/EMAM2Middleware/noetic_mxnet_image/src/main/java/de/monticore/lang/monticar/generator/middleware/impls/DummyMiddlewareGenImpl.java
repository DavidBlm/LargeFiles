/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.impls;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAPortInstanceSymbol;
import de.monticore.lang.monticar.generator.FileContent;
import de.monticore.lang.monticar.generator.middleware.helpers.FileHelper;
import de.monticore.lang.monticar.generator.middleware.helpers.NameHelper;
import de.monticore.lang.monticar.generator.middleware.helpers.TemplateHelper;
import de.monticore.lang.tagging._symboltable.TaggingResolver;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DummyMiddlewareGenImpl implements GeneratorImpl {

    private String generationTargetPath;

    @Override
    public List<File> generate(EMAComponentInstanceSymbol componentInstanceSymbol, TaggingResolver taggingResolver) throws IOException {

        List<File> res = new ArrayList<>();
        res.add(FileHelper.generateFile(generationTargetPath, generateCMake(componentInstanceSymbol)));
        res.add(FileHelper.generateFile(generationTargetPath, generateAdapterHeader(componentInstanceSymbol)));
        res.add(FileHelper.generateFile(generationTargetPath, generateAdapterCpp(componentInstanceSymbol)));
        return res;
    }


    private FileContent generateAdapterHeader(EMAComponentInstanceSymbol componentInstanceSymbol) {
        String name = NameHelper.getNameTargetLanguage(componentInstanceSymbol.getFullName());
        String content = TemplateHelper.getDummyAdapterTemplate()
                .replace("${compName}", name);

        FileContent res = new FileContent();
        res.setFileName("DummyAdapter_" + name + ".h");
        res.setFileContent(content);
        return res;
    }

    private FileContent generateAdapterCpp(EMAComponentInstanceSymbol componentInstanceSymbol){
        String name = NameHelper.getNameTargetLanguage(componentInstanceSymbol.getFullName());
        String content = "#include \"" + name +".h\"";
        FileContent res = new FileContent();
        res.setFileName("DummyAdapter_" + name + ".cpp");
        res.setFileContent(content);
        return res;
    }

    @Override
    public void setGenerationTargetPath(String path) {
        this.generationTargetPath = path;
    }

    @Override
    public boolean willAccept(EMAComponentInstanceSymbol componentInstanceSymbol) {
        return componentInstanceSymbol.getPortInstanceList().stream()
                .map(EMAPortInstanceSymbol::getMiddlewareSymbol)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .filter(mws -> mws.isKindOf(DummyMiddlewareSymbol.KIND))
                .count() > 0;
    }

    private FileContent generateCMake(EMAComponentInstanceSymbol componentInstanceSymbol) {
        FileContent res = new FileContent();
        String name = NameHelper.getNameTargetLanguage(componentInstanceSymbol.getFullName());
        String content = TemplateHelper.getDummyCmakeTemplate()
                .replace("${compName}", name);

        res.setFileName("CMakeLists.txt");
        res.setFileContent(content);
        return res;
    }
}
