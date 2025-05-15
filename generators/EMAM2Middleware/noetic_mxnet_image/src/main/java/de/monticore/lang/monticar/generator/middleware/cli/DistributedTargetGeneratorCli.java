/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.cli;

import com.google.gson.Gson;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.embeddedmontiarc.tagging.middleware.ros.RosToEmamTagSchema;
import de.monticore.lang.embeddedmontiarc.tagging.middleware.mqtt.MqttToEmamTagSchema;
import de.monticore.lang.embeddedmontiarc.tagging.middleware.someip.SomeIPToEmamTagSchema;
import de.monticore.lang.monticar.emadl.generator.EMADLAbstractSymtab;
import de.monticore.lang.monticar.generator.middleware.DistributedTargetGenerator;
import de.monticore.lang.monticar.generator.middleware.impls.*;
import de.monticore.lang.monticar.generator.order.simulator.AbstractSymtab;
import de.monticore.lang.monticar.generator.roscpp.helper.TagHelper;
import de.monticore.lang.monticar.generator.mqtt.helper.MqttTagHelper;
import de.monticore.lang.monticar.generator.someip.helper.SomeIPTagHelper;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.se_rwth.commons.logging.Log;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class DistributedTargetGeneratorCli {

    private static final String helpString = "Parameters: <file path to config json> OR -r <raw json config string>\n\n" +
            "Schema of config json:\n" +
            "{\n" +
            "    'modelsDir':'<path to directory with EMAM models>',\n" +
            "    'outputDir':'<path to output directory for generated files>',\n" +
            "    'rootModel':'<fully qualified name of the root model>',\n" +
            "    'generators':['<identifier for first generator>', '<identifier for second generator>',...],\n" +
            "    'emadlBackend':'<deep-learning-framework backend. Options: MXNET, CAFFE2>'\n" +
            "    'writeTagFile':[ 'true' | 'false'(default) ] Writes a .tag file with all Middleware tags into the generated code\n" +
            "}\n\n" +
            "Generator Options:\n" +
            "Behaviour generators:\n" +
            "    'cpp': EMAM2CPP\n" +
            "    'emadlcpp': EMADL2CPP\n" +
            "Middleware generators:\n" +
            "    'roscpp': EMAM2Roscpp";

    public static final String GENERATOR_CPP = "cpp";
    public static final String GENERATOR_EMADL = "emadlcpp";
    public static final String GENERATOR_ROSCPP = "roscpp";
    public static final String GENERATOR_MQTT = "mqtt";
    public static final String GENERATOR_ODV = "odv";
    //ros2cpp is an alias for rclcpp
    public static final String GENERATOR_RCLCPP = "rclcpp";
    public static final String GENERATOR_ROS2CPP = "ros2cpp";
    public static final String GENERATOR_SOMEIP = "someip";

    private DistributedTargetGeneratorCli() {}

    public static void main(String[] args) {
        System.out.println(Arrays.toString(args));
        Gson gson = new Gson();
        CliParameters parameters = null;
        //File name only
        if(args.length == 1){

            String filePath = expandHomeDir(args[0]);
            try {
                parameters = CliParametersLoader.loadCliParameters(filePath);
            } catch (FileNotFoundException e) {
                Log.error("0x49E6A: Can not find specified config file: " + args[0] + "!");
            }

        }
        //raw mode
        else if(args.length == 2 && args[0].equals("-r")){
            parameters = gson.fromJson(args[1], CliParameters.class);
        }else{
            System.out.println(helpString);
        }

        if(parameters != null){
            runGenerator(parameters);
        }
    }

    private static Set<String> getGeneratorNames() {
        HashSet<String> res = new HashSet<>();
        res.add(GENERATOR_CPP);
        res.add(GENERATOR_EMADL);
        res.add(GENERATOR_ROSCPP);
        res.add(GENERATOR_MQTT);
        res.add(GENERATOR_ODV);
        res.add(GENERATOR_ROS2CPP);
        res.add(GENERATOR_RCLCPP);
        res.add(GENERATOR_SOMEIP);
        return res;
    }

    public static void runGenerator(CliParameters cliParameters) {
        if(cliParameters.getModelsDir() == null){
            Log.error("0xEB6FE: No models dir specified!");
            return;
        }
        String fullModelsDirPath = expandHomeDir(cliParameters.getModelsDir());

        Set<String> generators = cliParameters.getGenerators();
        if(generators.size() == 0){
            Log.error("0x6178E: No generator was specified!");
            return;
        }

        if(!Files.isDirectory(Paths.get(fullModelsDirPath))){
            Log.error("0x6444B: Models dir does not exist: " + fullModelsDirPath);
            return;
        }

        TaggingResolver taggingResolver;
        if (generators.contains(GENERATOR_EMADL)) {
            taggingResolver = EMADLAbstractSymtab.createSymTabAndTaggingResolver(fullModelsDirPath);
        }
        else{
            taggingResolver = AbstractSymtab.createSymTabAndTaggingResolver(fullModelsDirPath);
        }

        DistributedTargetGenerator generator = new DistributedTargetGenerator();
        generator.setGenerationTargetPath(cliParameters.getOutputDir());
        generator.setGenerateMiddlewareTags(cliParameters.getWriteTagFile());

        Set<String> validGenNames = getGeneratorNames();

        generators.forEach(genName -> {
            if (validGenNames.contains(genName)) {
                if(genName.equals(GENERATOR_ROS2CPP)) {
                    Log.warn("Using generator " + GENERATOR_RCLCPP + " since " + genName + " is an alias!");
                }else {
                    Log.warn("Using generator " + genName);
                }
            } else {
                Log.error("0xE28B6: Not a valid generator Name:" + genName  +".");
                return;
            }
        });

        EMAComponentInstanceSymbol componentInstanceSymbol = taggingResolver.<EMAComponentInstanceSymbol>resolve(cliParameters.getRootModel(), EMAComponentInstanceSymbol.KIND).orElse(null);

        if (componentInstanceSymbol == null) {
            String[] parts = cliParameters.getRootModel().split("\\.");
            String componentInstanceName = parts[parts.length - 1];
            if(Character.isUpperCase(componentInstanceName.charAt(0))){
                parts[parts.length - 1] = componentInstanceName.substring(0,1).toLowerCase() + componentInstanceName.substring(1);
                Log.warn("The given ComponentInstance name "  + componentInstanceName + " starts with an upper case letter(Invalid). Did you mean " + String.join(".", parts) + "?");
            }
            Log.error("0x5FFAE: The given component cannot be resolved.");
            return;
        }

        if (generators.contains(GENERATOR_CPP)) {
            generator.add(new CPPGenImpl(cliParameters.getModelsDir()), "cpp");
        }

        if (generators.contains(GENERATOR_EMADL)) {
            if(cliParameters.getEmadlBackend() != null && !cliParameters.getEmadlBackend().equals("")) {
                generator.add(new EMADLGeneratorImpl(fullModelsDirPath, cliParameters.getEmadlBackend()), "cpp");
            }else{
                Log.error("0x0D839: Generator EMADL was specified but no backend was selected!");
                return;
            }
        }

        if (generators.contains(GENERATOR_ROSCPP)) {
            generator.add(new RosCppGenImpl(), "roscpp");
            RosToEmamTagSchema.registerTagTypes(taggingResolver);
            TagHelper.resolveTags(taggingResolver, componentInstanceSymbol);
        }
        
        if (generators.contains(GENERATOR_MQTT)) {
            generator.add(new MqttGenImpl(), "mqtt");
            MqttToEmamTagSchema.registerTagTypes(taggingResolver);
            MqttTagHelper.resolveTags(taggingResolver, componentInstanceSymbol);
        }

        if (generators.contains(GENERATOR_RCLCPP) || generators.contains(GENERATOR_ROS2CPP)) {
            generator.add(new RclCppGenImpl(), "rclcpp");
            RosToEmamTagSchema.registerTagTypes(taggingResolver);
            TagHelper.resolveTags(taggingResolver, componentInstanceSymbol);
        }

        if (generators.contains(GENERATOR_ODV)) {
            generator.add(new ODVGenImpl(), "odv");
        }

        if (generators.contains(GENERATOR_SOMEIP)) {
            generator.add(new SomeIPGenImpl(), "someip");
            SomeIPToEmamTagSchema.registerTagTypes(taggingResolver);
            SomeIPTagHelper.resolveTags(taggingResolver, componentInstanceSymbol);
        }

        if (cliParameters.getClusteringParameters().isPresent()) {
            ClusteringParameters clusteringParameters = cliParameters.getClusteringParameters().get();
            generator.setClusteringParameters(clusteringParameters);

            clusteringParameters.getAlgorithmParameters().stream()
                    .filter(alg -> !alg.isValid())
                    .forEach(alg -> Log.error("Parameters for the algorithm " + alg.getName() + " are invalid!"));
        }

        try {
            generator.generate(componentInstanceSymbol, taggingResolver);
        } catch (IOException e) {
            Log.error("Error generating files", e);
        }
    }

    private static String expandHomeDir(String path) {
        if (path.startsWith("~")) {
            return System.getProperty("user.home") + path.substring(1);
        }
        return path;
    }
}
