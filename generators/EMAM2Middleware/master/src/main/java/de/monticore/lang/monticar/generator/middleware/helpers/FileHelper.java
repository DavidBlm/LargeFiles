/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.helpers;

import de.monticore.lang.monticar.generator.FileContent;
import de.se_rwth.commons.logging.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class FileHelper {

    public static File generateFile(String targetPath, FileContent fileContent) throws IOException {
        File f = new File(targetPath + fileContent.getFileName());
        Log.info(f.getName(), "FileCreation:");
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            if (!f.createNewFile()) {
                Log.error("File could not be created");
            }
        }
        BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(f));
        bufferedWriter.write(fileContent.getFileContent(), 0, fileContent.getFileContent().length());
        bufferedWriter.close();
        return f;
    }

    public static List<File> generateFiles(String targetPath, List<FileContent> fileContents) throws IOException {
        ArrayList<File> files = new ArrayList<>();
        for (FileContent fc : fileContents) {
            files.add(generateFile(targetPath, fc));
        }
        return files;
    }
}
