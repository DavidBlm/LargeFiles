package de.monticore.mlpipelines.automl.helper;

import com.google.common.io.Resources;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class FileLoader {
    public static List<String> loadResourceFile(String modelName) {
        URL url = Resources.getResource(modelName);
        try {
            Path path = Paths.get(url.toURI());
            return Files.readAllLines(path, Charset.defaultCharset());
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> loadFile(String pathString) {
        try {
            Path path = Paths.get(pathString);
            return Files.readAllLines(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String writeToFile(List<String> lines, String pathString) {
        try {
            Path path = Paths.get(pathString);
            Files.write(path, lines, StandardCharsets.UTF_8);
            return path.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
