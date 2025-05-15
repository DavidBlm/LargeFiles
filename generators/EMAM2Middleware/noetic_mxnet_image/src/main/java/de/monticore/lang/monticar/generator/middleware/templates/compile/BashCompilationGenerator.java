/* (c) https://github.com/MontiCore/monticore */
package de.monticore.lang.monticar.generator.middleware.templates.compile;


import de.monticore.lang.monticar.generator.middleware.templates.MiddlewareTemplates;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BashCompilationGenerator extends CompilationGenerator {

    @Override
    public String getContent() {
        return  MiddlewareTemplates.generateCompileBash(this);
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
        setAdditionalErrorMsg("roscore", defaultErrorMsg("ROS"));
        setAdditionalErrorMsg("ros2", defaultErrorMsg("ROS2"));
        return Arrays.asList("cmake","make");
    }

    @Override
    public String getFileName() {
        return "compile.sh";
    }

    @Override
    public String getNewlineDelimiter() {
        return "\n";
    }

    @Override
    public List<String> getPostSourceExecutables() {
        ArrayList<String> res = new ArrayList<>();
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
        if(useRos()){
            res.add("\"$ROS_HOME\"/setup.bash");
        }
        if(useRos2()){
            res.add("\"$ROS2_HOME\"/setup.bash");
        }
        return res;
    }

    @Override
    public List<String> getExecutables() {
        List<String> res = new ArrayList<>(Arrays.asList("cmake", "make"));
        if(useRos2() && useStructMsgs()){
            res.add("colcon");
        }
        return res;
    }

}
