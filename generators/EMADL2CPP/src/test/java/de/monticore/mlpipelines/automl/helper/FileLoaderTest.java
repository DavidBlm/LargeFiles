package de.monticore.mlpipelines.automl.helper;

import junit.framework.TestCase;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FileLoaderTest extends TestCase {

    public void testLoadResourceFile() {
        String modelName = "models/adanet/ResourceLoaderTest.txt";
        List<String> lines = FileLoader.loadResourceFile(modelName);
        assertEquals(2, lines.size());
        assertEquals("test", lines.get(0));
        assertEquals("test2", lines.get(1));
    }

    public void testLoadFile() {
        String modelName = "ResourceLoaderTest2.txt";

        String pathString = "";
        if (SystemUtils.IS_OS_WINDOWS) {
            pathString = getTempModelPathString(modelName);
        }
        else if (SystemUtils.IS_OS_LINUX) {
            pathString = getTempModelPathString(modelName);
            pathString = pathString.substring(0, 4) + "/" + pathString.substring(4);
        }
        else {
            throw new IllegalStateException("OS is not supported (support only for Windows or Linux).");
        }

        createDummyTempFile(pathString);

        List<String> lines = FileLoader.loadFile(pathString);
        assertEquals(3, lines.size());
        assertEquals("test", lines.get(0));
        assertEquals("test2", lines.get(1));
        assertEquals("test3", lines.get(2));
    }

    private void createDummyTempFile(String pathString) {
        deleteFileIfExists(pathString);
        createFileIfNotExists(pathString);
        List<String> lines = createFileContent();
        FileLoader.writeToFile(lines, pathString);
    }

    private static String getTempModelPathString(String modelName) {
        String tempDir = System.getProperty("java.io.tmpdir");
        String pathString = tempDir + modelName;
        return pathString;
    }

    public void testWriteToFile() {
        String modelName = "ResourceLoaderTest2.txt";

        String pathString = "";

        if (SystemUtils.IS_OS_WINDOWS) {
            pathString = getTempModelPathString(modelName);
        }
        else if (SystemUtils.IS_OS_LINUX) {
            pathString = getTempModelPathString(modelName);
            pathString = pathString.substring(0, 4) + "/" + pathString.substring(4);
        }
        else {
            throw new IllegalStateException("OS is not supported (support only for Windows or Linux).");
        }

        createDummyTempFile(pathString);

        List<String> lines2 = FileLoader.loadFile(pathString);
        assertEquals(3, lines2.size());
        assertEquals("test", lines2.get(0));
        assertEquals("test2", lines2.get(1));
        assertEquals("test3", lines2.get(2));
    }

    private void deleteFileIfExists(String path) {
        try {
            Files.deleteIfExists(Paths.get(path));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void createFileIfNotExists(String pathString) {
        Path path = Paths.get(pathString);
        if (!Files.exists(path)) {
            try {
                Files.createFile(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private List<String> createFileContent() {
        List<String> lines = new ArrayList<>();
        lines.add("test");
        lines.add("test2");
        lines.add("test3");
        return lines;
    }
}