/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.emadl.generator;

import static de.monticore.lang.monticar.generator.cpp.GeneratorCppCli.OPTION_FLAG_ALGEBRAIC;
import static de.monticore.lang.monticar.generator.cpp.GeneratorCppCli.OPTION_FLAG_ARMADILLO;
import static de.monticore.lang.monticar.generator.cpp.GeneratorCppCli.OPTION_FLAG_CHECK_MODEL_DIR;
import static de.monticore.lang.monticar.generator.cpp.GeneratorCppCli.OPTION_FLAG_CMAKE;
import static de.monticore.lang.monticar.generator.cpp.GeneratorCppCli.OPTION_FLAG_EXEC_LOGGING;
import static de.monticore.lang.monticar.generator.cpp.GeneratorCppCli.OPTION_FLAG_GEN_TCP_SERVER;
import static de.monticore.lang.monticar.generator.cpp.GeneratorCppCli.OPTION_FLAG_LIBRARY_INTERFACE;
import static de.monticore.lang.monticar.generator.cpp.GeneratorCppCli.OPTION_FLAG_SERVER_WRAPPER;
import static de.monticore.lang.monticar.generator.cpp.GeneratorCppCli.OPTION_FLAG_TESTS;
import static de.monticore.lang.monticar.generator.cpp.GeneratorCppCli.OPTION_FLAG_THREADING;
import static de.monticore.lang.monticar.generator.cpp.GeneratorCppCli.OPTION_IMPORT_ARMADILLO;
import static de.monticore.lang.monticar.generator.cpp.GeneratorCppCli.OPTION_MODELS_PATH;
import static de.monticore.lang.monticar.generator.cpp.GeneratorCppCli.OPTION_OUTPUT_NAME;
import static de.monticore.lang.monticar.generator.cpp.GeneratorCppCli.OPTION_ROOT_MODEL;
import static de.monticore.lang.monticar.generator.cpp.GeneratorCppCli.addBaseOptions;
import static de.monticore.lang.monticar.generator.cpp.GeneratorCppCli.addEMAM2CPPOptions;

import de.monticore.lang.monticar.cnnarch.generator.GenerationAbortedException;
import de.monticore.lang.monticar.emadl.modularcnn.tools.Randomizer;
import de.monticore.lang.monticar.generator.cpp.GeneratorCPP;
import de.se_rwth.commons.logging.Log;
import freemarker.template.TemplateException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.SystemUtils;

public class EMADLGeneratorCli {

    public static final Option OPTION_OUTPUT_PATH = Option.builder("o")
            .longOpt("output-dir")
            .desc("full path to output directory for tests e.g. C:\\Users\\vpupkin\\proj\\MyAwesomeAutopilot\\target\\gen-cpp\n" +
                    "default is ./target/generated-sources-emadl/")
            .hasArg(true)
            .required(false)
            .build();
    
    public static final Option OPTION_BACKEND = Option.builder("b")
            .longOpt("backend")
            .desc("deep-learning-framework backend. Options: MXNET, CAFFE2, GLUON, TENSORFLOW")
            .hasArg(true)
            .required(false)
            .build();

    public static final Option OPTION_TRACKING_CONFIGURATION = Option.builder("track")
            .longOpt("tracking-configuration")
            .desc("Path to the tracking configuration file")
            .hasArg(true)
            .required(false)
            .build();

    public static final Option OPTION_TRAINING_PYTHON_PATH = Option.builder("p")
            .longOpt("python")
            .desc("path to python. Default is /usr/bin/python, or python (PATH) for windows")
            .hasArg(true)
            .required(false)
            .build();

    public static final Option OPTION_RESTRAINED_TRAINING = Option.builder("f")
            .longOpt("forced")
            .desc("no training or a forced training. Options: y (a forced training), n (no training)")
            .hasArg(true)
            .required(false)
            .build();

    public static final Option OPTION_COMPILE = Option.builder("c")
            .longOpt("compile")
            .desc("Compile the generated c code. Options: y (compile), n (don't compile). Default is y")
            .hasArg(true)
            .required(false)
            .build();

    public static final Option OPTION_CUSTOM_FILES_PATH = Option.builder("cfp")
            .longOpt("custom-files-path")
            .desc("full path to directory with language folders with custom files C::\\Users\\vpupkin\\prok\\My>AwsomeAutopilot\\src")
            .hasArg(true)
            .required(false)
            .build();

    public static final Option OPTION_USE_DGL = Option.builder("dgl")
            .longOpt("useDgl")
            .desc("If you use GNN Training whether to use DGL Layers or your own Custom Layers")
            .hasArg(true)
            .required(false)
            .build();

    public static final Option OPTION_HELP = Option.builder("h")
            .longOpt("help")
            .desc("Show CLI parameters")
            .hasArg(false)
            .required(false)
            .build();

    public static final Option OPTION_COMPOSED_NETWORK_FILE = Option.builder("cnf")
            .longOpt("composed-network-file")
            .desc("Specify composed network file name")
            .hasArg(true)
            .required(false).build();


    public static final Option OPTION_ALLOW_DECOMPOSITION = Option.builder("ad")
            .longOpt("allow-decomposition")
            .desc("Allow decomposition of subnetworks after training")
            .hasArg(false)
            .required(false).build();

    public static final Option OPTION_DECOMPOSABLE_NETWORKS = Option.builder("dn")
            .longOpt("decomposable-networks")
            .desc("Networks that can be decomposed")
            .hasArg(true)
            .required(false).build();

    public static final Option OPTION_NO_DEBUG_LOG = Option.builder("ndl")
            .longOpt("no-debug-log")
            .desc("Debug Logging is disabled")
            .hasArg(false)
            .required(false).build();


    protected EMADLGeneratorCli() {}


    public static void main(String[] args) {
        Options options = getOptions();
        CommandLineParser parser = new DefaultParser();
        CommandLine cliArgs = parseArgs(options, parser, args);
        if (cliArgs != null) {
            runGenerator(cliArgs);
        }
    }

    public static Options getOptions() {
        Options options = new Options();
        addBaseOptions(options);
        addEMAM2CPPOptions(options);
        addEMADL2CPPOptions(options);
        return options;
    }

    // Add EMADL2CPP Options
    public static void addEMADL2CPPOptions(Options options) {
        options.addOption(OPTION_BACKEND);
        options.addOption(OPTION_TRACKING_CONFIGURATION);
        options.addOption(OPTION_RESTRAINED_TRAINING);
        options.addOption(OPTION_TRAINING_PYTHON_PATH);
        options.addOption(OPTION_COMPILE);
        options.addOption(OPTION_CUSTOM_FILES_PATH);
        options.addOption(OPTION_USE_DGL);
        options.addOption(OPTION_HELP);
        options.addOption(OPTION_COMPOSED_NETWORK_FILE);
        options.addOption(OPTION_ALLOW_DECOMPOSITION);
        options.addOption(OPTION_DECOMPOSABLE_NETWORKS);
        options.addOption(OPTION_NO_DEBUG_LOG);
    }

    protected static void printHelp(){
        System.err.println("Arguments:");
        System.err.println("\t -m <parent model path>");
        System.err.println("\t -r <root model including full package name>");
        System.err.println("\t [-o <output directory>]  e.g. \"./target/\"");
        System.err.println("\t [-b <used backend>]  e.g. \"MXNET\"");
        System.err.println("\t [-f <force/prevent training>]  e.g. \"UNSET\"");
        System.err.println("\t [-p <training path>]");
        System.err.println("\t [-c <compile>] e.g. \"y\"/\"n\"");
        System.err.println("\t [-cfp <custom file path>]");
        System.err.println("\t [-dgl <use dgl>] e.g. \"y\"/\"n\"");
        System.err.println("\t [-cnf <composed network file path>] e.g. \"./CompFile\"");
        System.err.println("\t [-ad]");
        System.err.println("\t [-dn <list of network that can be decomposed] e.g. Network,Net1,Net2");
        System.err.println("\t [-ndl disable debug log");

    }

    static CommandLine parseArgs(Options options, CommandLineParser parser, String[] args) {
        CommandLine cliArgs;
        try {
            cliArgs = parser.parse(options, args);

        } catch (ParseException e) {
            System.err.println("argument parsing exception: " + e.getMessage());
            printHelp();
            throw new RuntimeException("argument parsing exception: " + e.getMessage());
        }
        return cliArgs;
    }

    protected static void runGenerator(CommandLine cliArgs) {
        String rootModelName = cliArgs.getOptionValue(OPTION_ROOT_MODEL.getOpt());
        String outputPath = cliArgs.getOptionValue(OPTION_OUTPUT_PATH.getOpt());
        String backendString = cliArgs.getOptionValue(OPTION_BACKEND.getOpt());
        String forced = cliArgs.getOptionValue(OPTION_RESTRAINED_TRAINING.getOpt());
        String pythonPath = cliArgs.getOptionValue(OPTION_TRAINING_PYTHON_PATH.getOpt());
        String compile = cliArgs.getOptionValue(OPTION_COMPILE.getOpt());
        String useDgl = cliArgs.getOptionValue(OPTION_USE_DGL.getOpt());
        String composedNetworksFileName = cliArgs.getOptionValue(OPTION_COMPOSED_NETWORK_FILE.getOpt());
        boolean allowDecompositionPresent = cliArgs.hasOption(OPTION_ALLOW_DECOMPOSITION.getOpt());
        boolean noDebugLog = cliArgs.hasOption(OPTION_NO_DEBUG_LOG.getOpt());

        if (noDebugLog) {
            Log.initWARN();
        }

        String commaDecompNetworkList = null;
        String[] decompNetworkList = null;
        if (cliArgs.hasOption(OPTION_DECOMPOSABLE_NETWORKS.getOpt())){
            commaDecompNetworkList = cliArgs.getOptionValue(OPTION_DECOMPOSABLE_NETWORKS.getOpt());
            decompNetworkList = commaDecompNetworkList.replaceAll(" ","").split(",");
        }


        final String DEFAULT_BACKEND = "MXNET";
        final String DEFAULT_FORCED = "UNSET";
        final String DEFAULT_COMPILE = "y";
        final String DEFAULT_USE_DGL = "n";
        final String DEFAULT_COMPOSED_NETWORKS_FILE = "ComposedNetworks";
        boolean allowDecomposition = false;

        if (allowDecompositionPresent){
            allowDecomposition = true;
        }

        if (composedNetworksFileName == null){
            composedNetworksFileName =  DEFAULT_COMPOSED_NETWORKS_FILE + "_" + rootModelName + Randomizer.timeStamp();
        } else {
            composedNetworksFileName = composedNetworksFileName + "_" + rootModelName + Randomizer.timeStamp();
        }

        if (outputPath != null && outputPath != ""){
            composedNetworksFileName = outputPath + "/" + composedNetworksFileName;
        }

        if (backendString == null) {
            Log.warn("Backend not specified. Backend set to default value " + DEFAULT_BACKEND);
            backendString = DEFAULT_BACKEND;
        }

        Optional<Backend> backend = Backend.getBackendFromString(backendString);
        if (!backend.isPresent()){
            Log.warn("Specified backend " + backendString + " is not supported. Backend set to default value " + DEFAULT_BACKEND);
            backend = Backend.getBackendFromString(DEFAULT_BACKEND);
        }

        if (pythonPath == null) {
            pythonPath = SystemUtils.IS_OS_WINDOWS ? "python" : "/usr/bin/python";
        }

        if (forced == null) {
            forced = DEFAULT_FORCED;
        }
        else if (!forced.equals("y") && !forced.equals("n")) {
            Log.warn("Specified setting ("+forced+") for forcing/preventing training not supported. Set to default value " + DEFAULT_FORCED);
            forced = DEFAULT_FORCED;
        }

        EMADLGenerator generator = new EMADLGenerator(backend.get(), composedNetworksFileName);

        if (compile == null) {
            compile = DEFAULT_COMPILE;
        }
        else if(!compile.equals("y") && !compile.equals("n")) {
            Log.warn("Specified setting ("+compile+") for skipping the compilation not supported. Set to default value " + DEFAULT_COMPILE);
            compile = DEFAULT_COMPILE;
        }

        if (useDgl == null) {
            useDgl = DEFAULT_USE_DGL;
        }
        else if(!useDgl.equals("y") && !useDgl.equals("n")) {
            Log.warn("Specified setting ("+useDgl+") for using the DGL package not supported. Set to default value " + DEFAULT_USE_DGL);
            useDgl = DEFAULT_USE_DGL;
        }

        if (outputPath != null){
            generator.setGenerationTargetPath(outputPath);
        }

        generator.setGenerateCMake(true);


        // EMAM2CPP options
        GeneratorCPP emamGen = generator.getEmamGen();
        Path modelsDirPath = Paths.get(cliArgs.getOptionValue(OPTION_MODELS_PATH.getOpt()));
        if (cliArgs.hasOption(OPTION_CUSTOM_FILES_PATH.getOpt())) {
            generator.getEmadlFileHandler().setCustomFilesPath(cliArgs.getOptionValue(OPTION_CUSTOM_FILES_PATH.getOpt()));
        }
        if (cliArgs.hasOption(OPTION_USE_DGL.getOpt())) {
            generator.setUseDgl(cliArgs.getOptionValue(OPTION_USE_DGL.getOpt()).equals("y"));
        }
        emamGen.setUseAlgebraicOptimizations(false);
        emamGen.setUseThreadingOptimization(false);
        emamGen.setModelsDirPath(modelsDirPath);
//        emamGen.setGenerationTargetPath(outputPath); // done by EMADLGenerator
        emamGen.setGenerateTests(cliArgs.hasOption(OPTION_FLAG_TESTS.getOpt()));

        emamGen.setImportArmadillo(cliArgs.hasOption(OPTION_IMPORT_ARMADILLO.getLongOpt()));
        emamGen.setGenerateLibraryInterface(cliArgs.hasOption(OPTION_FLAG_LIBRARY_INTERFACE.getLongOpt()));
        emamGen.setGenerateServerAdapter(cliArgs.hasOption(OPTION_FLAG_GEN_TCP_SERVER.getLongOpt()));
        emamGen.setOutputName(cliArgs.getOptionValue(OPTION_OUTPUT_NAME.getOpt()));
        if (cliArgs.hasOption(OPTION_FLAG_ARMADILLO.getOpt())) {
            emamGen.useArmadilloBackend();
        }
        emamGen.setCheckModelDir(cliArgs.hasOption(OPTION_FLAG_CHECK_MODEL_DIR.getLongOpt()));
        emamGen.setGenerateServerWrapper(cliArgs.hasOption(OPTION_FLAG_SERVER_WRAPPER.getLongOpt()));

        emamGen.setUseAlgebraicOptimizations(cliArgs.hasOption(OPTION_FLAG_ALGEBRAIC.getLongOpt()));
        emamGen.setUseThreadingOptimization(cliArgs.hasOption(OPTION_FLAG_THREADING.getLongOpt()));
        emamGen.setExecutionLoggingActive(cliArgs.hasOption(OPTION_FLAG_EXEC_LOGGING.getLongOpt()));
        emamGen.setGenerateCMake(cliArgs.hasOption(OPTION_FLAG_CMAKE.getLongOpt()));
        // end EMAM2CPP options

        String fullModelPath = cliArgs.getOptionValue(OPTION_MODELS_PATH.getOpt()) + rootModelName;

        try {
            //Log.warn("Running generator for model: " + fullModelPath);
            generator.generate(cliArgs.getOptionValue(OPTION_MODELS_PATH.getOpt()), rootModelName, pythonPath, forced, compile.equals("y"), useDgl, allowDecomposition, decompNetworkList);
            //Log.warn("Generator run finished for model: " + fullModelPath);
        } catch (IOException e){
            String errMsg ="io error during generation"+ e;
            Log.error(errMsg);
            throw new RuntimeException(errMsg);
        } catch (TemplateException e){
            String errMsg = "template error during generation: "+ e;
            Log.error(errMsg);
            throw new RuntimeException(errMsg);
        } catch (GenerationAbortedException e) {
            String message = String.format("Generation aborted: %s", e.getMessage());
            Log.error(message);
        }
    }

}