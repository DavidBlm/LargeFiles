package de.monticore.mlpipelines.util;

import static junit.framework.TestCase.assertEquals;
import static org.junit.Assert.assertTrue;

import de.monticore.mlpipelines.configuration.ExperimentConfiguration;
import de.monticore.mlpipelines.configuration.MontiAnnaContext;
import de.monticore.mlpipelines.tracking.TrackerFactory;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;

public class TestUtil {

    public static MontiAnnaContext initialiseContext(
            Path parentModelPath,
            final String rootModelName,
            final ExperimentConfiguration experimentConfiguration1) {
        MontiAnnaContext.getInstance().initContext(parentModelPath, rootModelName, experimentConfiguration1, new TrackerFactory(null));
        return MontiAnnaContext.getInstance();

    }

    /***
     * refactored from AbtractSymtabTest
     */
    public static void checkFilesAreEqual(Path generationPath, Path resultsPath, List<String> fileNames) {
        for (String fileName : fileNames) {
            File genFile = new File(generationPath.toString() + "/" + fileName);
            File fileTarget = new File(resultsPath.toString() + "/" + fileName);
            assertTrue(areBothFilesEqual(genFile, fileTarget));
        }
    }

    public static boolean areBothFilesEqual(File file1, File file2) {
        if (!file1.exists()) {
            Assert.fail("file does not exist: " + file1.getAbsolutePath());
            return false;
        }
        if (!file2.exists()) {
            Assert.fail("file does not exist: " + file2.getAbsolutePath());
            return false;
        }
        List<String> lines1;
        List<String> lines2;
        try {
            lines1 = Files.readAllLines(file1.toPath());
            lines2 = Files.readAllLines(file2.toPath());
        } catch (IOException e) {
            e.printStackTrace();
            Assert.fail("IO error: " + e.getMessage());
            return false;
        }
        lines1 = discardEmptyLines(lines1);
        lines1 = discardCopyrightNotice(lines1);
        lines2 = discardEmptyLines(lines2);
        lines2 = discardCopyrightNotice(lines2);

        String lines1AsString = String.join("\n", lines1);
        String lines2AsString = String.join("\n", lines2);

        if (lines1.size() != lines2.size()) {
            Assert.assertEquals(
                    "files have different number of lines: "
                            + file1.getAbsolutePath()
                            + " has " + lines1
                            + " lines and " + file2.getAbsolutePath() + " has " + lines2 + " lines",
                    lines1AsString,
                    lines2AsString
            );
            return false;
        }

        int len = lines1.size();
        for (int i = 0; i < len; i++) {
            String l1 = lines1.get(i).trim();
            String l2 = lines2.get(i).trim();
            if (!l1.equals(l2))
                Assert.assertEquals("files differ in line " + i + ": "
                                + file1.getAbsolutePath()
                                + " has " + l1
                                + " and " + file2.getAbsolutePath() + " has " + l2,
                        String.join("\n\n", l1, lines1AsString),
                        String.join("\n\n", l2, lines2AsString)
                );
        }
        return true;
    }

    private static List<String> discardEmptyLines(List<String> lines) {
        return lines.stream()
                .map(String::trim)
                .filter(l -> !l.isEmpty())
                .collect(Collectors.toList());
    }

    private static List<String> discardCopyrightNotice(List<String> lines) {
        return lines
                .stream()
                .filter(s -> !s.contains("(c) https://github.com/MontiCore/monticore"))
                .collect(Collectors.toList());

    }

    public static void checkFindingsCount() {
        int size = Log.getFindings().size();
        assertEquals(String.format("Expected %s findings but got %s:\n%s\n", 0, size,
                        Log.getFindings().stream().map(Finding::toString).collect(Collectors.joining("\n"))),
                0L, size);
    }
}
