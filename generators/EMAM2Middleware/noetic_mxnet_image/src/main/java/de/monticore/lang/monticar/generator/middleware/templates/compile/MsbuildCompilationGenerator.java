/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.templates.compile;

import de.monticore.lang.monticar.generator.middleware.templates.MiddlewareTemplates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MsbuildCompilationGenerator extends WindowsCompilationGenerator {

    @Override
    public String getContent() {
        return MiddlewareTemplates.generateCompileMsbuild(this);
    }


    @Override
    public boolean supportsRos() {
        return true;
    }

    @Override
    public boolean supportsRos2() {
        return true;
    }

    @Override
    public List<String> getAdditionalPathDirs() {
        setAdditionalErrorMsg("vcvars64.bat", defaultErrorMsg("msbuild"));
        if(useRos()){
            setAdditionalErrorMsg("ros", defaultErrorMsg("ROS"));
        }
        if(useRos2()) {
            setAdditionalErrorMsg("ros2", defaultErrorMsg("ROS2"));
        }
        return Arrays.asList("cmake","msbuild");
    }


    @Override
    public String getFileName() {
        return "compileMsbuild.bat";
    }

    @Override
    public List<String> getPostSourceExecutables() {
        List<String> res = new ArrayList<>();
        res.add("msbuild");
        if(useRos()){
            res.add("roscore");
        }
        if(useRos2()){
            res.add("ros2");
        }
        return res;
    }

    @Override
    public List<String> getEnvironmentFiles() {
        List<String> res = new ArrayList<>();
        res.add("vcvars64.bat");
        if(useRos()){
            res.add("%ROS_HOME%\\setup.bat");
        }
        if(useRos2()){
            res.add("%ROS2_HOME%\\local_setup.bat");
        }
        return res;
    }

    @Override
    public List<String> getExecutables() {
        List<String> res = new ArrayList<>(Arrays.asList("cmake","vcvars64.bat"));
        if(useRos2() && useStructMsgs()){
            res.add("colcon");
        }
        return res;
    }

    @Override
    public WinGenKind getKind() {
        return WinGenKind.MSBUILD;
    }
}
