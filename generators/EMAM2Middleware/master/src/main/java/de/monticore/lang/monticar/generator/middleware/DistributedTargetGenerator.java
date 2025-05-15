/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.embeddedmontiarc.helper.SymbolPrinter;
import de.monticore.lang.monticar.clustering.AutomaticClusteringHelper;
import de.monticore.lang.monticar.clustering.ClusteringResult;
import de.monticore.lang.monticar.clustering.ClusteringResultList;
import de.monticore.lang.monticar.clustering.FlattenArchitecture;
import de.monticore.lang.monticar.generator.middleware.cli.ClusteringParameters;
import de.monticore.lang.monticar.generator.middleware.cli.ResultChoosingStrategy;
import de.monticore.lang.monticar.generator.middleware.helpers.ClusterFromTagsHelper;
import de.monticore.lang.monticar.generator.middleware.helpers.FileHelper;
import de.monticore.lang.monticar.generator.middleware.helpers.NameHelper;
import de.monticore.lang.monticar.generator.middleware.helpers.RosHelper;
import de.monticore.lang.monticar.generator.middleware.impls.*;
import de.monticore.lang.monticar.generator.middleware.templates.compile.CompilationGenerator;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class DistributedTargetGenerator{
    private boolean generateMiddlewareTags = false;
    private ClusteringResultList clusteringResults = new ClusteringResultList();
    private CMakeGenerator generatorBlueprint = new CMakeGenerator();
    private String generationTargetPath;

    public boolean isGenerateMiddlewareTags() {
        return generateMiddlewareTags;
    }

    public void setGenerateMiddlewareTags(boolean generateMiddlewareTags) {
        this.generateMiddlewareTags = generateMiddlewareTags;
    }

    private ClusteringParameters clusteringParameters;

    public DistributedTargetGenerator() {
    }

    public ClusteringParameters getClusteringParameters() {
        return clusteringParameters;
    }

    public void setClusteringParameters(ClusteringParameters clusteringParameters) {
        this.clusteringParameters = clusteringParameters;
    }

    public void setGenerationTargetPath(String path) {
        String res = path;
        if(res.endsWith("/") || res.endsWith("\\")){
            res = res.substring(0,res.length() - 1);
        }
        if(res.endsWith("/src") || res.endsWith("\\src")){
            res = res.substring(0, res.length() - 4);
        }
        generationTargetPath = res + "/";
    }

    public List<File> generate(EMAComponentInstanceSymbol genComp, TaggingResolver taggingResolver) throws IOException {
        EMAComponentInstanceSymbol componentInstanceSymbol = preprocessing(genComp);

        generatorBlueprint.setGenerationTargetPath(generationTargetPath);

        StarBridgeGenerator completeGenerator = new CMakeGenerator();
        completeGenerator.setGenerationTargetPath(generationTargetPath + "src/");

        List<EMAComponentInstanceSymbol> clusterSubcomponents = ClusterFromTagsHelper.getClusterSubcomponents(componentInstanceSymbol);
        if (clusterSubcomponents.isEmpty()) {
            clusterSubcomponents = Arrays.asList(componentInstanceSymbol);
        }
        for (EMAComponentInstanceSymbol clusterECIS : clusterSubcomponents) {
            String nameTargetLanguage = NameHelper.getNameTargetLanguage(clusterECIS.getFullName());
            StateGenerator stateGenerator = new StateGenerator(createFullGenerator(nameTargetLanguage), clusterECIS, taggingResolver);
            completeGenerator.add(stateGenerator, nameTargetLanguage);
        }

        List<File> files = new ArrayList<>();

        boolean useStructMsgs = false;
        if (generatorBlueprint.getGeneratorImpls().stream().anyMatch(gi -> gi instanceof RosCppGenImpl)) {
            RosMsgGenImpl msgGen = new RosMsgGenImpl(false);
            if(msgGen.willAccept(componentInstanceSymbol)) {
                completeGenerator.add(msgGen, "struct_msgs", 0);
                useStructMsgs = true;
            }
        }

        if (generatorBlueprint.getGeneratorImpls().stream().anyMatch(gi -> gi instanceof RclCppGenImpl)) {
            RosMsgGenImpl msgGen = new RosMsgGenImpl(true);
            if(msgGen.willAccept(componentInstanceSymbol)) {
                StarBridgeGenerator newGen = new StarBridgeGenerator();
                newGen.setGenerationTargetPath(generationTargetPath + "src/");
                newGen.add(completeGenerator, "comps");
                completeGenerator = newGen;
                completeGenerator.add(msgGen, "struct_msgs");
                useStructMsgs = true;
            }
        }

        if(generateMiddlewareTags){
            MiddlewareTagGenImpl middlewareTagGen = new MiddlewareTagGenImpl();
            middlewareTagGen.setGenerationTargetPath(generationTargetPath + "emam/");
            middlewareTagGen.setClusteringResults(clusteringResults);
            files.addAll(middlewareTagGen.generate(componentInstanceSymbol,taggingResolver));

            File file = saveModel(componentInstanceSymbol);

            files.add(file);
        }

        files.addAll(completeGenerator.generate(componentInstanceSymbol, taggingResolver));
        files.addAll(generateCompileScripts(useStructMsgs));
        return files;
    }

    private File saveModel(EMAComponentInstanceSymbol componentInstance) throws IOException {
        String name = componentInstance.getName().substring(0,1).toUpperCase() + componentInstance.getName().substring(1);
        String pathname = generationTargetPath + "/emam/model/" + name + ".emam";
        Log.info("Writing component into file: " + pathname, "files");
        String modelData = SymbolPrinter.printEMAComponentInstanceAsEMAComponent(componentInstance);
        File file = new File(pathname);
        file.getParentFile().mkdirs();
        FileUtils.write(file, modelData,"UTF-8");
        return file;
    }

    private EMAComponentInstanceSymbol preprocessing(EMAComponentInstanceSymbol genComp) {
        EMAComponentInstanceSymbol componentInstanceSymbol = genComp;
        if(clusteringParameters != null){
            //Flatten
            if(clusteringParameters.getFlatten()){
                if(clusteringParameters.getFlattenLevel().isPresent()){
                    Integer level = clusteringParameters.getFlattenLevel().get();
                    componentInstanceSymbol = FlattenArchitecture.flattenArchitecture(genComp, new HashMap<>(), level);
                }else {
                    componentInstanceSymbol = FlattenArchitecture.flattenArchitecture(genComp);
                }
                System.out.println("Subcomponents after flatten: " + componentInstanceSymbol.getSubComponents().size());
            }

            //Cluster
            if(clusteringParameters.getAlgorithmParameters().size() > 0) {
                clusteringResults = ClusteringResultList.fromParametersList(componentInstanceSymbol, clusteringParameters.getAlgorithmParameters(), clusteringParameters.getMetric());
                Optional<Integer> nOpt = clusteringParameters.getNumberOfClusters();
                for(ClusteringResult c : clusteringResults){
                    String prefix = nOpt.isPresent() && !c.hasNumberOfClusters(nOpt.get()) ? "[IGNORED]" : "";
                    c.saveAsJson(generationTargetPath +"emam/", "clusteringResults.json");
                    System.out.println(prefix + "Score was " + c.getScore() + " for " + c.getParameters().toString());
                }

                Optional<ClusteringResult> clusteringOpt;
                if(nOpt.isPresent() && clusteringParameters.getChooseBy().equals(ResultChoosingStrategy.bestWithFittingN)){
                    clusteringOpt = clusteringResults.getBestResultWithFittingN(nOpt.get());
                }else{
                    clusteringOpt = clusteringResults.getBestResultOverall();
                }

                if(clusteringOpt.isPresent()){
                    ClusteringResult clusteringResult = clusteringOpt.get();
                    System.out.println("Best score was " + clusteringResult.getScore() + " for " + clusteringResult.getParameters().toString());
                    AutomaticClusteringHelper.annotateComponentWithRosTagsForClusters(componentInstanceSymbol, clusteringResult.getClustering());
                }
            }
        }

        fixComponentInstance(componentInstanceSymbol);
        return componentInstanceSymbol;
    }

    private GeneratorImpl createFullGenerator(String subdir) {
        MiddlewareGenerator res = new MiddlewareGenerator();
        res.setGenerationTargetPath(generationTargetPath + "src/" + (subdir.endsWith("/") ? subdir : subdir + "/"));

        generatorBlueprint.getGeneratorImpls().forEach(gen -> res.add(gen, generatorBlueprint.getImplSubdir(gen)));

        return res;
    }

    private void fixComponentInstance(EMAComponentInstanceSymbol componentInstanceSymbol) {
        RosHelper.fixRosConnectionSymbols(componentInstanceSymbol, generatorBlueprint.getGeneratorImpls().stream().anyMatch(g -> g instanceof RclCppGenImpl));
    }

    protected List<File> generateCompileScripts(boolean useStructMsgs){
        List<File> res = new ArrayList<>();

        boolean useRos = generatorBlueprint.getGeneratorImpls().stream().anyMatch(impl -> impl instanceof RosCppGenImpl);
        boolean useRos2 = generatorBlueprint.getGeneratorImpls().stream().anyMatch(impl -> impl instanceof RclCppGenImpl);

        List<CompilationGenerator> generators = CompilationGenerator.getInstanceOfAllGenerators();
        generators.forEach(g -> g.setUseRos(useRos));
        generators.forEach(g -> g.setUseRos2(useRos2));
        generators.forEach(g -> g.setUseStructMsgs(useStructMsgs));

        generators.stream()
                .peek(g -> g.setUseRos(useRos))
                .peek(g -> g.setUseRos2(useRos2))
                .filter(CompilationGenerator::isValid)
                .forEach(g -> {
                    try {
                        res.addAll(FileHelper.generateFiles(generationTargetPath ,g.getCompilationScripts()));
                    } catch (IOException e) {
                        Log.error("0xD4BAA: Error generating compile scripts!", e);
                    }
                });

        for (File f : res) {
            try {
                f.setExecutable(true, false);
            } catch (Exception e) {
                Log.warn("Could not set permissions of " + f.getAbsolutePath());
            }
        }


        return res;
    }

    public void add(GeneratorImpl generator, String subdir) {
        generatorBlueprint.add(generator, subdir);
    }
}
