package de.monticore.lang.monticar.emadl.generator;

import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAComponentSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstantiationSymbol;
import de.monticore.lang.monticar.cnnarch.generator.DataPathConfigParser;
import de.monticore.lang.monticar.emadl._cocos.DataPathCocos;
import de.monticore.lang.monticar.emadl.generator.utils.DependencyInstaller;
import de.monticore.lang.monticar.emadl.generator.utils.MavenSettings;
import de.monticore.lang.monticar.emadl.tagging.artifacttag.DatasetArtifactSymbol;
import de.monticore.lang.monticar.emadl.tagging.artifacttag.LayerArtifactParameterSymbol;
import de.monticore.lang.monticar.emadl.tagging.dltag.DataPathSymbol;
import de.monticore.lang.monticar.emadl.tagging.dltag.LayerPathParameterSymbol;
import de.monticore.lang.tagging._symboltable.TagSymbol;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.se_rwth.commons.logging.Log;
import de.monticore.lang.monticar.semantics.Constants;
import de.monticore.lang.monticar.semantics.util.BasicLibrary;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Tagging {

    private EMADLGenerator emadlGen;

    public Tagging(EMADLGenerator emadlGen){
        this.emadlGen =  emadlGen;
    }

    protected TaggingResolver getSymTabAndTaggingResolver() {
        BasicLibrary.extract();
        return EMADLAbstractSymtab.createSymTabAndTaggingResolver(emadlGen.getEmadlFileHandler().getCustomFilesPath(), emadlGen.getEmadlFileHandler().getPythonPath(), emadlGen.getBackend(), emadlGen.getComposedNetworkFilePath(), emadlGen.getEmadlFileHandler()
                        .getModelsPath(),
                Constants.SYNTHESIZED_COMPONENTS_ROOT, BasicLibrary.BASIC_LIBRARY_ROOT);
    }

    protected HashMap getLayerPathParameterTags(TaggingResolver taggingResolver, EMAComponentSymbol component, EMAComponentInstanceSymbol instance) {
        List<TagSymbol> instanceTags = new LinkedList<>();
        boolean isChildComponent = instance.getEnclosingComponent().isPresent();

        if (isChildComponent) {
            // get all instantiated components of parent
            List<EMAComponentInstantiationSymbol> instantiationSymbols = (List<EMAComponentInstantiationSymbol>) instance
                    .getEnclosingComponent().get().getComponentType().getReferencedSymbol().getSubComponents();

            // filter corresponding instantiation of instance and add tags
            instantiationSymbols.stream().filter(e -> e.getName().equals(instance.getName())).findFirst()
                    .ifPresent(symbol -> instanceTags.addAll(taggingResolver.getTags(symbol, LayerPathParameterSymbol.KIND)));
        }

        List<TagSymbol> tags = !instanceTags.isEmpty() ? instanceTags
                : (List<TagSymbol>) taggingResolver.getTags(component, LayerPathParameterSymbol.KIND);

        HashMap layerPathParameterTags = new HashMap();
        if (!tags.isEmpty()) {
            for (TagSymbol tag : tags) {
                LayerPathParameterSymbol layerPathParameterSymbol = (LayerPathParameterSymbol) tag;
                layerPathParameterTags.put(layerPathParameterSymbol.getId(), layerPathParameterSymbol.getPath());
            }
            // TODO: Replace warinings with errors, until then use this method
            emadlGen.stopGeneratorIfWarning();
            Log.warn("Tagging info for LayerPathParameter symbols was found.");
        }
        return layerPathParameterTags;
    }

    protected HashMap getLayerArtifactParameterTags(TaggingResolver taggingResolver, EMAComponentSymbol component, EMAComponentInstanceSymbol instance){
        List<TagSymbol> instanceTags = new LinkedList<>();
        boolean isChildComponent = instance.getEnclosingComponent().isPresent();

        if (isChildComponent) {
            // get all instantiated components of parent
            List<EMAComponentInstantiationSymbol> instantiationSymbols = (List<EMAComponentInstantiationSymbol>) instance
                    .getEnclosingComponent().get().getComponentType().getReferencedSymbol().getSubComponents();

            // filter corresponding instantiation of instance and add tags
            instantiationSymbols.stream().filter(e -> e.getName().equals(instance.getName())).findFirst()
                    .ifPresent(symbol -> instanceTags.addAll(taggingResolver.getTags(symbol, LayerArtifactParameterSymbol.KIND)));
        }

        List<TagSymbol> tags = !instanceTags.isEmpty() ? instanceTags
                : (List<TagSymbol>) taggingResolver.getTags(component, LayerArtifactParameterSymbol.KIND);

        HashMap layerArtifactParameterTags = new HashMap();
        File localRepo = new MavenSettings().getLocalRepository();
        if (!tags.isEmpty()) {
            for(TagSymbol tag: tags) {
                LayerArtifactParameterSymbol layerArtifactParameterSymbol = (LayerArtifactParameterSymbol) tag;
                String path = emadlGen.getEmadlFileHandler().getArtifactDestination(localRepo.toString(), layerArtifactParameterSymbol.getArtifact(), layerArtifactParameterSymbol.getJar());
                layerArtifactParameterTags.put(layerArtifactParameterSymbol.getId(), path);
            }
            emadlGen.stopGeneratorIfWarning();
            Log.warn("Tagging info for LayerArtifact symbols was found.");
        }
        return layerArtifactParameterTags;
    }

    protected List<String> getDataPaths(TaggingResolver taggingResolver, EMAComponentSymbol component, EMAComponentInstanceSymbol instance) {
        List<TagSymbol> instanceTags = new LinkedList<>();
        boolean isChildComponent = instance.getEnclosingComponent().isPresent();

        if (isChildComponent) {
            // get all instantiated components of parent
            List<EMAComponentInstantiationSymbol> instantiationSymbols = (List<EMAComponentInstantiationSymbol>) instance
                    .getEnclosingComponent().get().getComponentType().getReferencedSymbol().getSubComponents();

            // filter corresponding instantiation of instance and add tags
            instantiationSymbols.stream().filter(e -> e.getName().equals(instance.getName())).findFirst()
                    .ifPresent(symbol -> {
                        instanceTags.addAll(taggingResolver.getTags(symbol, DataPathSymbol.KIND));
                        instanceTags.addAll(taggingResolver.getTags(symbol, DatasetArtifactSymbol.KIND));
                    });
        }

        // instance tags have priority
        List<TagSymbol> tags;
        if (!instanceTags.isEmpty()) {
            tags = instanceTags;
        }
        else {
            tags = Stream
                    .concat(taggingResolver.getTags(component, DataPathSymbol.KIND).stream(), taggingResolver.getTags(component, DatasetArtifactSymbol.KIND).stream())
                    .collect(Collectors.toList());
        }
        List<String> dataPaths = new LinkedList<>();

        if (!tags.isEmpty()) {
            for(TagSymbol tagSymbol : tags){
                if (tagSymbol instanceof DataPathSymbol) {
                    DataPathSymbol dataPathSymbol = (DataPathSymbol) tagSymbol;
                    DataPathCocos.check(dataPathSymbol);

                    String dataPath = dataPathSymbol.getPath();
                    dataPaths.add(dataPath);
                    Log.warn("Tagging info for DataPath symbol was found, ignoring data_paths.txt: " + dataPath);
                }
                else {
                    DatasetArtifactSymbol datasetArtifactSymbol = (DatasetArtifactSymbol) tagSymbol;

                    String groupId = datasetArtifactSymbol.getGroupId();
                    String artifactId = datasetArtifactSymbol.getArtifactId();
                    String version = datasetArtifactSymbol.getVersion();
                    DependencyInstaller.installDependency(groupId, artifactId, version);

                    for(Pair<Path, String> mavenPackage : DependencyInstaller.resolveDependencies(groupId, artifactId, version)){
                        Path dataPathPrefix = mavenPackage.getKey().resolve(mavenPackage.getValue() + "-dataset");
                        try {
                            emadlGen.getEmadlFileHandler().unzipJar(dataPathPrefix.toString());
                        } catch (IOException e){
                            e.printStackTrace();
                        }

                        if(dataPathPrefix.resolve("training_data").toFile().exists()){
                            dataPaths.add(dataPathPrefix.resolve("training_data").toString());
                        } else if(dataPathPrefix.resolve( "data").toFile().exists()){
                            dataPaths.add(dataPathPrefix.resolve("data").toString());
                        } else {
                            System.out.println(dataPathPrefix.resolve( "data").toString());
                            throw new RuntimeException("No valid dataset artifact structure found for artifact " + groupId + ":" + artifactId + ":" + version);
                        }
                    }
                }
            }
            emadlGen.stopGeneratorIfWarning();


        }
        else {
            Path dataPathDefinition = Paths.get(emadlGen.getEmadlFileHandler().getModelsPath(), "data_paths.txt");
            if (dataPathDefinition.toFile().exists()) {
                DataPathConfigParser newParserConfig = new DataPathConfigParser(emadlGen.getEmadlFileHandler().getModelsPath() + "data_paths.txt");
                dataPaths.add(newParserConfig.getDataPath(component.getFullName()));
            } else {
                Log.warn("No data path definition found in " + dataPathDefinition + " found: "
                        + "Set data path to default ./data path");
                dataPaths.add("data");
            }
        }

        return dataPaths;
    }
}
