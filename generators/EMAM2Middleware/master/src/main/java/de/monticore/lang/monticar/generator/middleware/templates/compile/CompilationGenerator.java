/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.templates.compile;

import de.monticore.lang.monticar.generator.FileContent;

import java.util.*;

public abstract class CompilationGenerator {
    private boolean useRos = false;
    private boolean useRos2 = false;
    private boolean useStructMsgs = false;
    private Map<String, String> additionalErrorMsg = new HashMap<>();

    public abstract boolean supportsRos();

    public abstract boolean supportsRos2();

    public boolean isValid() {
        return (supportsRos() || !useRos()) && (supportsRos2() || !useRos2());
    }

    public void setAdditionalErrorMsg(String executable, String errorMsg) {
        additionalErrorMsg.put(executable, errorMsg);
    }

    public String getAdditionalErrorMsg(String executable) {
        return additionalErrorMsg.getOrDefault(executable, defaultErrorMsg(executable));
    }

    public abstract List<String> getAdditionalPathDirs();

    public abstract String getFileName();

    public abstract String getNewlineDelimiter();

    public boolean useRos() {
        return useRos;
    }

    public void setUseRos(boolean useRos) {
        this.useRos = useRos;
    }

    public boolean useRos2() {
        return useRos2;
    }

    public void setUseRos2(boolean useRos2) {
        this.useRos2 = useRos2;
    }

    public boolean useStructMsgs() {
        return useStructMsgs;
    }

    public void setUseStructMsgs(boolean useStructMsgs) {
        this.useStructMsgs = useStructMsgs;
    }

    public List<FileContent> getCompilationScripts() {
        List<FileContent> res = new ArrayList<>();
        res.add(new FileContent(getContent(), getFileName()));
        if(useRos2() && useStructMsgs()) {
            res.add(new FileContent("", "src/comps/COLCON_IGNORE"));
        }
        return res;
    }

    public abstract String getContent();

    public abstract List<String> getPostSourceExecutables();

    public abstract List<String> getEnvironmentFiles();

    public abstract List<String> getExecutables();

    public static List<CompilationGenerator> getInstanceOfAllGenerators() {
        List<CompilationGenerator> res = new ArrayList<>();
        res.add(new BashCompilationGenerator());
        res.add(new MingwCompilationGenerator());
        res.add(new MsbuildCompilationGenerator());
        return res;
    }

    public String defaultErrorMsg(String executable) {
        return "Try setting the environment variable " + executable + "_HOME to the base of your installation or adding it to your PATH!";
    }
}
