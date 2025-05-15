package de.monticore.mlpipelines.util;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Collections;
import java.util.stream.Collectors;

public class ResourcesUtil {

    /***
     * copied from EMADLGenerator
     */
    public static boolean copySchemaFilesFromResource(
            final String rootSchemaModelPath,
            final String generationTargetPath) {
        try {
            String jarPath = ResourcesUtil.class.getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .toURI()
                    .getPath();

            String target_path = generationTargetPath;
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
            } catch (UnsupportedOperationException e) {
                System.out.println("this should only be printed if the generator is run unpacked");
                return false;
            }
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
