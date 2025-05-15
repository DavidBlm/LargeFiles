/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl.generator;

        import de.monticore.lang.monticar.cnnarch.generator.CNNArchGenerator;
        import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
        import de.monticore.lang.monticar.generator.FileContent;
        import de.monticore.lang.monticar.generator.cmake.CMakeConfig;
        import de.monticore.lang.monticar.generator.cmake.CMakeFindModule;
        import de.monticore.lang.tagging._symboltable.TaggingResolver;

        import java.util.ArrayList;
        import java.util.List;

public class CNNArch2NoBackend extends CNNArchGenerator {
    CMakeConfig cMakeConfig = new CMakeConfig("");

    public CNNArch2NoBackend() {

    }

    @Override
    public CMakeConfig getCmakeConfig() {
        return this.cMakeConfig;
    }

    //check cocos with CNNArchCocos.checkAll(architecture) before calling this method.
    public List<FileContent> generateStrings(TaggingResolver taggingResolver, ArchitectureSymbol architecture){
        List<FileContent> fileContentMap = new ArrayList<>();

        return fileContentMap;
    }

    public boolean check(ArchitectureSymbol architecture) {
        //return architectureSupportChecker.check(architecture) && layerSupportChecker.check(architecture);
        return false;
    }


    public List<FileContent> generateCMakeContent(String rootModelName) {
        // model name should start with a lower case letter. If it is a component, replace dot . by _
        rootModelName = rootModelName.replace('.', '_').replace('[', '_').replace(']', '_');
        rootModelName = rootModelName.substring(0, 1).toLowerCase() + rootModelName.substring(1);

        this.cMakeConfig = new CMakeConfig(rootModelName);
        this.cMakeConfig.addModuleDependency(new CMakeFindModule("Armadillo", true));

        List<FileContent> fileContentMap = new ArrayList<>();
        for (FileContent fileContent : cMakeConfig.generateCMakeFiles()){
            fileContentMap.add(fileContent);
        }
        return fileContentMap;
    }


}
