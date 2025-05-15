package de.monticore.lang.monticar.emadl.generator;

import com.google.common.io.Resources;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.EMAComponentSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstanceSymbol;
import de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.instanceStructure.EMAComponentInstantiationSymbol;
import de.monticore.lang.monticar.cnnarch._symboltable.ArchitectureSymbol;
import de.monticore.lang.monticar.cnnarch.generator.DataPathConfigParser;
import de.monticore.lang.monticar.cnnarch.generator.WeightsPathConfigParser;
import de.monticore.lang.monticar.emadl._cocos.DataPathCocos;
import de.monticore.lang.monticar.emadl.generator.modularcnn.NetworkCompositionHandler;
import de.monticore.lang.monticar.emadl.generator.utils.ChecksumGenerator;
import de.monticore.lang.monticar.emadl.tagging.artifacttag.DatasetArtifactSymbol;
import de.monticore.lang.monticar.emadl.tagging.dltag.DataPathSymbol;
import de.monticore.lang.monticar.generator.FileContent;
import de.monticore.lang.tagging._symboltable.TagSymbol;
import de.monticore.lang.tagging._symboltable.TaggingResolver;
import de.monticore.symboltable.Symbol;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FileHandler {

    private EMADLGenerator emadlGen;

    private String adaNetUtils = "./src/main/resources/AdaNet/";
    private String modelsPath;
    private String customFilesPath = "";
    private String pythonPath = "";
    private String rootConfigFileName = "";

    private String composedNetworksFilePath = "";
    private Set<EMAComponentInstanceSymbol> instanceVault = null;

    private EMAComponentInstanceSymbol vaultBuildingInstance = null;


    public FileHandler(EMADLGenerator emadlGen, String composedNetworksFilePath){
        this.emadlGen =  emadlGen;
        this.composedNetworksFilePath = composedNetworksFilePath;
    }

    public Set<EMAComponentInstanceSymbol> getInstanceVault(){
        return this.instanceVault;
    }



    protected String getAdaNetUtils() {
        return adaNetUtils;
    }

    protected void setAdaNetUtils(String adaNetUtils) {
        this.adaNetUtils = adaNetUtils;
    }

    protected String getModelsPath() {
        return modelsPath;
    }

    protected void setModelsPath(String modelsPath) {
        if (!(modelsPath.endsWith("/"))) {
            this.modelsPath = modelsPath + "/";
        }
        else {
            this.modelsPath = modelsPath;
        }
    }

    protected String getCustomFilesPath() { return  customFilesPath; }

    protected void setCustomFilesPath(String customPythonFilesPath) {
        if (!(customPythonFilesPath.endsWith("/"))){
            this.customFilesPath = customPythonFilesPath + "/";
        }
        else {
            this.customFilesPath = customPythonFilesPath;
        }
    }

    public String getPythonPath() {return pythonPath;}

    protected void setPythonPath (String pythonPath){
        if(!pythonPath.startsWith("/")){
            pythonPath = "/" + pythonPath;
        }
        this.pythonPath = pythonPath;
    }

    public void setComposedNetworksFilePath(String path){
        this.composedNetworksFilePath = path;
    }

    public String getComposedNetworksFilePath(){
        return this.composedNetworksFilePath;
    }

    protected void setVaultBuildingInstance(EMAComponentInstanceSymbol vaultBuildingInstance){
        this.vaultBuildingInstance = vaultBuildingInstance;
    }



    protected void copyPythonFilesFromResource(String folder) throws URISyntaxException, IOException {
        // this function copys the passed folder to the generation target
        // important for AdaNet
        String jarPath = getClass().getProtectionDomain()
                .getCodeSource()
                .getLocation()
                .toURI()
                .getPath();
        String target_path = emadlGen.getGenerationTargetPath() + folder;
        if (!target_path.endsWith("/")) {
            target_path = target_path + '/';
        }
        Files.createDirectories(Paths.get(target_path));
        URI uri = URI.create("jar:file:" + jarPath);
        try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
            for (Path path : Files.walk(fs.getPath(folder)).filter(Files::isRegularFile).collect(Collectors.toList())) {
                if (path.toString().endsWith(".py")) {
                    String destination = target_path + path.getFileName();

                    Files.copy(path, Paths.get(destination), StandardCopyOption.REPLACE_EXISTING);
                }
            }
            setAdaNetUtils(emadlGen.getGenerationTargetPath()+"/"+folder+ "/");
        }catch (UnsupportedOperationException e){
            System.out.println("this should only be printed if the generator is run unpacked");
        }
    }

    protected String getDataPath(TaggingResolver taggingResolver, EMAComponentSymbol component, EMAComponentInstanceSymbol instance) {
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
        String dataPath;

        if (!tags.isEmpty()) {
            if (tags.get(0) instanceof DataPathSymbol) {
                DataPathSymbol dataPathSymbol = (DataPathSymbol) tags.get(0);
                DataPathCocos.check(dataPathSymbol);

                dataPath = dataPathSymbol.getPath();
                Log.warn("Tagging info for DataPath symbol was found, ignoring data_paths.txt: " + dataPath);
            }
            else {
                DatasetArtifactSymbol datasetArtifactSymbol = (DatasetArtifactSymbol) tags.get(0);

                String localRepo = System.getProperty("user.home") + File.separator + ".m2" + File.separator + "repository";
                dataPath = getArtifactDestination(localRepo, datasetArtifactSymbol.getArtifact(), datasetArtifactSymbol.getJar()) + File.separator + "training_data";
                Log.warn("Tagging info for DatasetArtifact symbol was found, ignoring data_paths.txt: " + dataPath);

            }
            emadlGen.stopGeneratorIfWarning();


        }
        else {
            Path dataPathDefinition = Paths.get(getModelsPath(), "data_paths.txt");
            if (dataPathDefinition.toFile().exists()) {
                DataPathConfigParser newParserConfig = new DataPathConfigParser(getModelsPath() + "data_paths.txt");
                dataPath = newParserConfig.getDataPath(component.getFullName());
            } else {
                Log.warn("No data path definition found in " + dataPathDefinition + " found: "
                        + "Set data path to default ./data path");
                dataPath = "data";
            }
        }
        return dataPath;
    }

    protected String getWeightsPath(EMAComponentSymbol component, EMAComponentInstanceSymbol instance) {
        String weightsPath;

        Path weightsPathDefinition = Paths.get(getModelsPath(), "weights_paths.txt");
        if (weightsPathDefinition.toFile().exists()) {
            WeightsPathConfigParser newParserConfig = new WeightsPathConfigParser(getModelsPath() + "weights_paths.txt");
            weightsPath = newParserConfig.getWeightsPath(component.getFullName());
        } else {
            Log.debug("No weights path definition found in " + weightsPathDefinition + ": "
                    + "No pretrained weights will be loaded.", this.getClass().getName());
            weightsPath = null;
        }
        return weightsPath;
    }

    public String getChecksumForFile(String filePath) throws IOException {
        Path wiki_path = Paths.get(filePath);
        MessageDigest md5;

        try {
            md5 = MessageDigest.getInstance("MD5");
            md5.update(Files.readAllBytes(wiki_path));
            byte[] digest = md5.digest();

            return hex(digest);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "No_Such_Algorithm_Exception";
        }
    }

    protected static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            result.append(String.format("%02X", aByte));
        }
        return result.toString();
    }

    protected String getArtifactDestination(String localRepo, String artifact, String jar) {
        String destinationPath = localRepo + File.separator + artifact + File.separator + jar;
        try {
            unzipJar(destinationPath);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return destinationPath;
    }


    protected String getChecksumForLargerFile(String filePath) throws IOException {
        try {
            return (new File(filePath)).lastModified() + "";
        } catch (Exception e) {
            e.printStackTrace();
            return "Exception_calculating_hash_large_file";
        }
    }

    protected List<File> generateCMakeFiles(EMAComponentInstanceSymbol componentInstanceSymbol) {
        List<File> files = new ArrayList<>();
        if (componentInstanceSymbol != null) {
            emadlGen.getCmakeConfig().getCMakeListsViewModel().setCompName(componentInstanceSymbol.getFullName().replace('.', '_').replace('[', '_').replace(']', '_'));
        }
        List<FileContent> contents = emadlGen.getCmakeConfig().generateCMakeFiles();
        try {
            for (FileContent content : contents)
                files.add(generateFile(content));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return files;
    }

    protected File generateFile(FileContent fileContent) throws IOException {
        File f = new File(emadlGen.getGenerationTargetPath() + fileContent.getFileName());
        Log.debug("FileCreation:" + f.getName(), this.getClass().getName());
        boolean contentEqual = false;
        //Actually slower than just saving and overwriting the file
        /*if (f.exists()) {
            String storedFileContent = new String(Files.readAllBytes(f.toPath()));
            if (storedFileContent.equals(fileContent.getFileContent())) {
                contentEqual = true;
            }
        } else*/
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            if (!f.createNewFile()) {
                Log.error("File could not be created: "+ f.getAbsolutePath());
            }
        }

        if (!contentEqual) {
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(f));
            bufferedWriter.write(fileContent.getFileContent(), 0, fileContent.getFileContent().length());
            bufferedWriter.close();
        }
        return f;
    }

    private Set<EMAComponentInstanceSymbol> buildInstanceVault(EMAComponentInstanceSymbol emaComponentInstanceSymbol, String forced){
        Set<EMAComponentInstanceSymbol> newInstanceVault = null;

        NetworkCompositionHandler networkCompositionHandler = new NetworkCompositionHandler(this.composedNetworksFilePath, getModelsPath(), new LinkedHashMap<>(),emadlGen.getBackend(), new LinkedHashMap<>());
        newInstanceVault = findInstancesRecursively(emaComponentInstanceSymbol, networkCompositionHandler);

        networkCompositionHandler = new NetworkCompositionHandler(this.composedNetworksFilePath, getModelsPath(), newInstanceVault,
                emadlGen.getEmadlCNNHandler().getCachedComposedArchitectureSymbols(), this.emadlGen.getBackend(), emadlGen.getEmadlCNNHandler().getComposedNetworkStructures());
        ArrayList<EMAComponentInstanceSymbol> processedInstances = networkCompositionHandler.processComponentInstances(newInstanceVault);

        for (EMAComponentInstanceSymbol instanceSymbol : processedInstances){
            networkCompositionHandler.resolveArchitectureSymbolOfReferencedSymbol(instanceSymbol);
        }

        return newInstanceVault;
    }

    private Set<EMAComponentInstanceSymbol> findInstancesRecursively(EMAComponentInstanceSymbol emaComponentInstanceSymbol, NetworkCompositionHandler networkCompositionHandler){
        Set<EMAComponentInstanceSymbol> instances = new HashSet<>();

        Map<String, Collection<Symbol>> symbols = emaComponentInstanceSymbol.getSpannedScope().getLocalSymbols();
        if (symbols != null && symbols.size() > 0){
            for (String key : symbols.keySet()){
                ArrayList<Symbol> symWrapperList = (ArrayList<Symbol>) symbols.get(key);
                if ( symWrapperList != null && symWrapperList.size() > 0 && symWrapperList.get(0) instanceof EMAComponentInstanceSymbol){
                    EMAComponentInstanceSymbol foundInstance = (EMAComponentInstanceSymbol) symWrapperList.get(0);
                    Optional<ArchitectureSymbol> architectureSymbol = networkCompositionHandler.resolveArchitectureSymbolOfInstance(foundInstance);

                    if (architectureSymbol.isPresent()){
                        instances.add(foundInstance);
                    }

                    instances.add(foundInstance);
                    instances.addAll(findInstancesRecursively(foundInstance, networkCompositionHandler));
                }
            }
        }
        return instances;
    }

    protected List<File> generateFiles(TaggingResolver taggingResolver, EMAComponentInstanceSymbol emaComponentSymbol, String pythonPath, String forced) throws IOException {
        Set<EMAComponentInstanceSymbol> allInstances = new HashSet<>();

        //INFO: Used for information gathering to build correct architectures and C++/Python output files
        this.instanceVault = buildInstanceVault(emaComponentSymbol, forced);

        List<FileContent> fileContents = emadlGen.generateStrings(taggingResolver, emaComponentSymbol, allInstances, forced);
        List<File> generatedFiles = new ArrayList<>();

        Log.info("Generating adapters ...", EMADLGenerator.class.getName());
        emadlGen.getEmamGen().generateAdapters(fileContents, emaComponentSymbol);

        for (FileContent fileContent : fileContents) {
            generatedFiles.add(emadlGen.getEmamGen().generateFile(fileContent));
        }

        // train
        Map<String, String> fileContentMap = new HashMap<>();
        for (FileContent f : fileContents) {
            fileContentMap.put(f.getFileName(), f.getFileContent());
        }

        List<FileContent> fileContentsTrainingHashes = new ArrayList<>();
        List<String> newHashes = new ArrayList<>();

        NetworkCompositionHandler networkCompositionHandler = new NetworkCompositionHandler(this.composedNetworksFilePath, getModelsPath(),this.instanceVault,
                emadlGen.getEmadlCNNHandler().getCachedComposedArchitectureSymbols(), emadlGen.getBackend(), emadlGen.getEmadlCNNHandler().getComposedNetworkStructures());
        ArrayList<EMAComponentInstanceSymbol> networks = networkCompositionHandler.processComponentInstances(allInstances);

        for (EMAComponentInstanceSymbol componentInstance : networks) {
            Optional<ArchitectureSymbol> architecture = networkCompositionHandler.resolveArchitectureSymbolOfInstance(componentInstance);

            if (!architecture.isPresent() || forced.equals("n")) {
                continue;
            }

            if ( (!architecture.isPresent() && !networkCompositionHandler.isComposedNet(componentInstance)) || networkCompositionHandler.isPartOfComposedNet(componentInstance) ) {
                continue;
            }

            if (architecture.get().getDataPaths().isEmpty()) {
                throw new RuntimeException("No dataset found. Please specify a dataset using the Network.tag file");
            }

            if (!computeChanges(architecture, componentInstance) && !forced.equals("y")) {
                Log.error("Training of model " + componentInstance.getFullName() + " skipped");
                continue;
            }

            String parsedFullName = componentInstance.getFullName().
                    substring(0, 1).toLowerCase() + componentInstance.getFullName().substring(1).replaceAll("\\.", "_");
            String trainerScriptName = "CNNTrainer_" + parsedFullName + ".py";
            Log.warn("Training of model " + trainerScriptName);
            String trainingPath = emadlGen.getGenerationTargetPath() + trainerScriptName;
            if (Files.exists(Paths.get(trainingPath))) {
                ProcessBuilder pb = new ProcessBuilder(Arrays.asList(pythonPath, trainingPath)).inheritIO();
                Process p = pb.start();

                int exitCode = 0;
                try {
                    exitCode = p.waitFor();
                }
                catch(InterruptedException e) {
                    String errMsg = "Training aborted: exit code " + exitCode;

                    Log.error(errMsg);
                    throw new RuntimeException(errMsg);
                }

                if(exitCode != 0) {
                    String errMsg = "Training failed: exit code " + exitCode;

                    Log.error(errMsg);
                    throw new RuntimeException(errMsg);
                }
            } else {
                Log.warn("Training file " + trainingPath + " not found.");
            }
        }

        for (FileContent fileContent : fileContentsTrainingHashes) {
            generatedFiles.add(emadlGen.getEmamGen().generateFile(fileContent));
        }

        if (emadlGen.isGenerateCMake())
            generateCMakeFiles(emaComponentSymbol);

        return generatedFiles;
    }

    protected void unzipJar(String destinationPath) throws IOException {
        File destination = new File(destinationPath);
        if (destination.exists() && destination.isDirectory()) {
            return;
        }
        destination.mkdir();

        File jar = new File(destination.getAbsolutePath() + ".jar");
        if (!jar.exists()) {
            Log.error("dependency " + destination.getAbsolutePath() + " could not be found");
        }
        ZipFile zipFile = new ZipFile(jar);

        Enumeration jarFileEntries = zipFile.entries();
        while (jarFileEntries.hasMoreElements()) {
            ZipEntry jarEntry = (ZipEntry) jarFileEntries.nextElement();
            File outputFile = new File(destination.getAbsolutePath(), jarEntry.getName());
            outputFile.getParentFile().mkdirs();

            if (outputFile.isDirectory()) {
                outputFile.mkdirs();
            }
            else {
                BufferedInputStream is = new BufferedInputStream(zipFile.getInputStream(jarEntry));
                int currentByte;
                byte[] data = new byte[1024];

                FileOutputStream fos = new FileOutputStream(outputFile);
                BufferedOutputStream dest = new BufferedOutputStream(fos, 1024);

                // read and write until last byte is encountered
                while ((currentByte = is.read(data, 0, 1024)) != -1) {
                    dest.write(data, 0, currentByte);
                }
                dest.flush();
                dest.close();
                is.close();
            }

        }
    }

    public void fixArmadilloImports(List<FileContent> fileContents) {
        for (FileContent fileContent : fileContents) {
            fileContent.setFileContent(fileContent.getFileContent()
                    .replaceFirst("#include \"armadillo.h\"",
                            "#include \"armadillo\""));
        }
    }

    public List<String> getContentOf(Collection<FileContent> fileContents, String fileName) {
        return fileContents
                .stream()
                .filter(fc -> fc.getFileName().equals(fileName))
                .map(FileContent::getFileContent)
                .collect(Collectors.toList());
    }

    private File getNewHashFile(){
        return Paths.get(emadlGen.getGenerationTargetPath(), "hashes", "new_hashes.json").toFile();
    }

    public void setRootConfigurationFile() {
        Log.info("RootConfFileValue: " + rootConfigFileName, "ROOT_CONFIG_FILE_SET");
        if (!rootConfigFileName.equals("")) {
            return;
        }
        Log.info("Trying to find root conf", "ROOT_CONFIG_FILE_SET");
        try {
            ArrayList<String> confFiles = new ArrayList<>();
            Stream<Path> files  = Files.list(Paths.get(getModelsPath() + "modularNetworks/"));
            //files.forEach(file -> Log.info("Available File: " + file.toString(),"ROOT_CONFIG_FILE_SET"));
            files.filter(file -> file.toString().endsWith(".conf")).forEach(file -> confFiles.add(file.toString()));
            if (confFiles.size() > 0){
                for (String s: confFiles) {
                    Log.info("Available Config File: " + s,"ROOT_CONFIG_FILE_SET");
                }
                Log.info("Setting RootConfFile File to: " + confFiles.get(0),"ROOT_CONFIG_FILE_SET");
                rootConfigFileName = confFiles.get(0);
            } else{
                Log.info("No RootConfigFile found.","ROOT_CONFIG_FILE_SET");
            }
        } catch (IOException e){
            Log.error("RootConfigFileError: " + e.getMessage());

        }
    }

    public String getConfigFilename(String mainComponentConfigFilename, String componentConfigFilename, String instanceConfigFilename) {
        Log.info("CompNames(main|comp|inst): "+ mainComponentConfigFilename + " | " + componentConfigFilename + " | " + instanceConfigFilename ,"CONFIG_FILE_SEARCH");
        Log.info("RootConfFileValue: " + rootConfigFileName, "CONFIG_FILE_SEARCH");
        if (!rootConfigFileName.equals("")) {
            return rootConfigFileName;
        }

        String trainConfigFilename;
        if (Files.exists(Paths.get(getModelsPath() + instanceConfigFilename + ".conf"))) {
            trainConfigFilename = instanceConfigFilename;
        } else if (Files.exists(Paths.get(getModelsPath() + componentConfigFilename + ".conf"))) {
            trainConfigFilename = componentConfigFilename;
        } else if (Files.exists(Paths.get(getModelsPath() + mainComponentConfigFilename + ".conf")) ||
                Files.exists(Paths.get(getModelsPath() + mainComponentConfigFilename + ".emadl"))) {
            trainConfigFilename = mainComponentConfigFilename;
        } else {
            return null;
        }

        return trainConfigFilename;
    }

    protected boolean copySchemaFilesFromResource(String rootSchemaModelPath) {
        try {
            String jarPath = getClass().getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();

            String target_path = emadlGen.getGenerationTargetPath();
            if (!target_path.endsWith("/")) {
                target_path = target_path + '/';
            }
            URI uri = URI.create("jar:file:" + jarPath);
            try (FileSystem fs = FileSystems.newFileSystem(uri, Collections.emptyMap())) {
                for (Path path : Files.walk(fs.getPath(rootSchemaModelPath)).filter(Files::isRegularFile).collect(Collectors.toList())) {
                    if (path.toString().endsWith(".scm") || path.toString().endsWith(".ema")) {
                        Path destination = Paths.get(target_path + path);
                        Files.createDirectories(destination.getParent());
                        Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            } catch (UnsupportedOperationException e){
                System.out.println("this should only be printed if the generator is run unpacked");
                return false;
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public String readResource(final String fileName, Charset charset) {
        try {
            return Resources.toString(Resources.getResource(fileName), charset);

        } catch (IllegalArgumentException e) {
            System.err.println("Resource file " + fileName + " not found");
            throw new RuntimeException("Resource file " + fileName + " not found");
        } catch (IOException e) {
            System.err.println("IO Error occurred");
            throw new RuntimeException("IO Error occurred");
        }
    }


    public File createTempScript() throws IOException {
        File tempScript = File.createTempFile("script", null);
        if (!SystemUtils.IS_OS_WINDOWS) {
            try {
                Writer streamWriter = new OutputStreamWriter(new FileOutputStream(
                        tempScript));
                PrintWriter printWriter = new PrintWriter(streamWriter);
                final String currentDir = System.getProperty("user.dir");


                printWriter.println("#!/bin/bash");
                printWriter.println("cd " + emadlGen.getGenerationTargetPath());
                printWriter.println("mkdir -p build");
                printWriter.println("cd build");
                printWriter.println("rm -r -f *");
                printWriter.println("pwd");
                printWriter.println("cp -R " + currentDir + "/model .");
                printWriter.println("cmake ..");
                printWriter.println("make");

                printWriter.close();
            } catch (Exception e) {
                Log.error(e.getMessage());
            }
        } else {
            try {
                Writer streamWriter = new OutputStreamWriter(new FileOutputStream(
                        tempScript));
                PrintWriter printWriter = new PrintWriter(streamWriter);

                printWriter.println("cd " + emadlGen.getGenerationTargetPath());
                printWriter.println("if exist build del /F /Q /S build");
                printWriter.println("mkdir build");
                printWriter.println("cd build");
                printWriter.println("cmake ..");
                printWriter.println("cmake --build .  --config release");

                printWriter.close();
            } catch (Exception e) {
                Log.error(e.getMessage());
            }
        }

        return tempScript;
    }

    /**
     * This function hashes all dataset files, emadl and cnnt files.
     * The hashes are packed into a JSON file and get saved to disk.
     * @param architecture
     * @param componentInstance
     * @return JSONObject with the individual hashes of all files.
     */
    private JSONObject generateHashFile(Optional<ArchitectureSymbol> architecture, EMAComponentInstanceSymbol componentInstance) {

        String componentConfigFilename = "hashes/hashes.json";
        JSONObject newDatasetsHashes = new JSONObject();

        List<File> datasets = architecture.get().getDataPaths().stream().map(File::new).map(
                file -> file.listFiles((FilenameFilter) new WildcardFileFilter("*.json"))
        ).filter(Objects::nonNull).flatMap(Arrays::stream).collect(Collectors.toList());
        if(!datasets.isEmpty()){
            String content = "";
            for(File child : datasets){
                try(FileInputStream inputStream = new FileInputStream(child.getAbsolutePath())) {
                    content = IOUtils.toString(inputStream, Charset.defaultCharset());
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException("Exception while reading dataset metadata " + child.getAbsolutePath());
                }

                JSONObject datasetMetadata = new JSONObject(content);

                String datasetFilename = datasetMetadata.getString("id") + "." + datasetMetadata.getString("filetype");
                datasetMetadata.put("hash", ChecksumGenerator.getChecksumForFileSHA1(child.getParent() + File.separator + datasetFilename));
                datasetMetadata.put("path", new File(child.getParentFile(), datasetFilename).getAbsolutePath());
                newDatasetsHashes.put(datasetMetadata.getString("id"), datasetMetadata);
            }
        } else {
            Log.info("No metadata information about datasets found. Fallback to hashing all files.", this.getClass().getName());
            datasets = architecture.get().getDataPaths().stream().map(File::new).map(
                    File::listFiles).filter(Objects::nonNull).flatMap(Arrays::stream).collect(Collectors.toList());
            for (File child : datasets) {
                if(child.isDirectory()){
                    continue;
                }
                String newChecksum = ChecksumGenerator.getChecksumForFileSHA1(child.getPath());

                JSONObject datasetMetadata = new JSONObject();

                String baseFileName = child.getName().split("\\.")[0];
                if(baseFileName.equals("testing") || baseFileName.equals("test")){
                    datasetMetadata.put("testing", true);
                }
                datasetMetadata.put("hash", newChecksum);
                datasetMetadata.put("path", child.getAbsolutePath());
                newDatasetsHashes.put(child.getName(), datasetMetadata);
            }
        }

        String mainComponentConfigFilename = componentInstance.getComponentType().getFullName().replaceAll("\\.", "/");
        String instanceConfigFilename = componentInstance.getFullName().replaceAll("\\.", "/") + "_" + componentInstance.getName();
        String configFilename = getConfigFilename(mainComponentConfigFilename, componentInstance.getFullName().replaceAll("\\.", "/"), instanceConfigFilename);
        String configFilepath = getModelsPath().concat(configFilename);
        String emadlPath = configFilepath.concat(".emadl");
        String cnntPath = configFilepath.concat(".conf");

        JSONObject newHashes = new JSONObject();
        newHashes.put("datasets", newDatasetsHashes);
        newHashes.put("emadl", ChecksumGenerator.getChecksumForFileSHA1(emadlPath));
        newHashes.put("cnnt", ChecksumGenerator.getChecksumForFileSHA1(cnntPath));

        File newHashFile = getNewHashFile();
        if(newHashFile.exists()){
            newHashFile.delete();
        }

        try {
            generateFile(new FileContent(newHashes.toString(), componentConfigFilename));
        } catch (IOException e){
            e.printStackTrace();
            throw new RuntimeException("Could not write to file " + newHashFile.getAbsolutePath());
        }

        return newHashes;
    }

    /**
     * Computes the changes between the last execution and the files of this execution.
     * It will read the old_hashes.json file internally, where the old hashes are stored.
     * @param architecture
     * @param componentInstance
     * @return change status of datasets and emadl files. True if something changed, false if nothing changed.
     */
    private boolean computeChanges(Optional<ArchitectureSymbol> architecture, EMAComponentInstanceSymbol componentInstance){
        JSONObject oldHashes;
        boolean executionNecessary = false;

        try {
            String fileContent = "";
            try(FileInputStream hashInputStream = new FileInputStream(emadlGen.getGenerationTargetPath() + "hashes/hashes.json")) {
                fileContent = IOUtils.toString(hashInputStream, Charset.defaultCharset());
            }
            oldHashes = new JSONObject(fileContent);
        } catch (FileNotFoundException e){
            Log.info("Execution necessary: Hashfile not found.", this.getClass().getName());
            this.generateHashFile(architecture, componentInstance);
            executionNecessary = true;
            oldHashes = null;
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Exception while reading old hash files");
        }

        JSONObject oldDatasetHashes;
        if(oldHashes != null){
            oldDatasetHashes = oldHashes.getJSONObject("datasets");
        } else {
            oldDatasetHashes = null;
        }

        JSONObject newHashes = this.generateHashFile(architecture, componentInstance);

        for(String key : newHashes.keySet()){
            if(!key.equals("datasets")){
                if(oldHashes == null || !newHashes.getString(key).equalsIgnoreCase(oldHashes.getString(key))){
                    Log.info("Input file " + key + " did change. Execution necessary", this.getClass().getName());
                    executionNecessary = true;
                } else {
                    Log.debug("Input file " + key + " did not change.", this.getClass().getName());
                }
            }
        }

        JSONObject newDatasetHashes = newHashes.getJSONObject("datasets");
        LinkedList<String> executionOrder = new LinkedList<>();

        JSONObject retrainingConf = new JSONObject();

        // Use kind of insertion sort to resolve references and determine execution order
        for(String id : newDatasetHashes.keySet()) {
            boolean testing = false;
            try {
                testing = newDatasetHashes.getJSONObject(id).getBoolean("testing");
            } catch (JSONException e){
                Log.debug("Found no testing flag. This does usually happen when using the old dataset style.", this.getClass().getName());
            }

            // Ignore testing dataset
            if(!testing){
                this.calculateExecutionOrder(executionOrder, newDatasetHashes, id);
            } else {
                retrainingConf.put("testing", newDatasetHashes.getJSONObject(id));
            }
        }

        Log.info("Determined execution order: " + executionOrder, this.getClass().getName());

        JSONArray changes = new JSONArray();
        for (String id : executionOrder){
            // Compute change
            JSONObject changeContent = new JSONObject();
            changeContent.put("id", id);
            changeContent.put("path", newDatasetHashes.getJSONObject(id).getString("path"));
            if(oldDatasetHashes == null) {
                Log.info("Old dataset hashes not found. Execution required.", this.getClass().getName());
                changeContent.put("retraining", true);
                executionNecessary = true;
            } else if(!oldDatasetHashes.has(id)){
                Log.info("Dataset " + id + " has been added: Execution required.", this.getClass().getName());
                changeContent.put("retraining", true);
                executionNecessary = true;
            } else if(!newDatasetHashes.getJSONObject(id).getString("hash")
                    .equalsIgnoreCase(oldDatasetHashes.getJSONObject(id).getString("hash"))){
                Log.info("Dataset " + id + " has changed: Execution required.", this.getClass().getName());
                changeContent.put("retraining", true);
                executionNecessary = true;
            } else {
                changeContent.put("retraining", false);
                Log.debug("Dataset hasn't changed.", this.getClass().getName());
            }

            changes.put(changeContent);
        }

        retrainingConf.put("changes", changes);

        // Write changes to file
        String componentChangeFilename = "/conf/retraining.json";
        try {
            generateFile(new FileContent(retrainingConf.toString(), componentChangeFilename));
        } catch (IOException e){
            e.printStackTrace();
            throw new RuntimeException("Could not write to file " + componentChangeFilename);
        }

        if(!executionNecessary){
            Log.info("Execution not necessary: No files changed.", this.getClass().getName());
        }
        return executionNecessary;
    }

    private int calculateExecutionOrder(List<String> executionOrder, JSONObject datasets, String idToCheck){

        // If id is already in executionOrder list, return the position.
        int idIndex = executionOrder.indexOf(idToCheck);
        if(idIndex != -1){
            return idIndex;
        }

        JSONObject dataset = datasets.getJSONObject(idToCheck);

        String reference = null;
        try {
            reference = dataset.getString("references");
        } catch (JSONException e){
            Log.debug("Found no reference flag. This does usually happen when using the old dataset style.", this.getClass().getName());
        }

        // If reference is null, add dataset at position 0 (has to be trained first).
        if (reference == null) {
            Log.debug("Dataset " + idToCheck + " has no reference. Will use it as base.", this.getClass().getName());
            executionOrder.add(0, idToCheck);
            return 0;
        }

        // Resolve reference
        Log.debug("Dataset " + idToCheck + " has the reference " + reference + ". Trying to resolve reference...", this.getClass().getName());
        int referenceIndex = executionOrder.indexOf(reference);
        if (referenceIndex == -1) {
            if(datasets.has(reference)){
                referenceIndex = calculateExecutionOrder(executionOrder, datasets, reference);
                if(referenceIndex != -1){
                    executionOrder.add( referenceIndex + 1, idToCheck);
                }
                return referenceIndex + 1;
            } else {
                throw new RuntimeException("Reference " + reference + " of dataset not found. Please check your dataset configuration.");
            }
        } else {
            executionOrder.add(referenceIndex + 1, idToCheck);
            return referenceIndex + 1;
        }
    }



















}
