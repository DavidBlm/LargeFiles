<!-- (c) https://github.com/MontiCore/monticore -->
# Adding a new middleware generator

## EmbeddedMontiArc
To save the additional middleware information in the Symbol Table, the field `PortSymbol::middlewareSymbol` is used.
Add a new class for your middleware that extends [MiddlewareSymbol](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/languages/EmbeddedMontiArc/blob/master/src/main/java/de/monticore/lang/embeddedmontiarc/tagging/middleware/MiddlewareSymbol.java)(Example: [RosConnectionSymbol](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/languages/EmbeddedMontiArc/blob/master/src/main/java/de/monticore/lang/embeddedmontiarc/tagging/middleware/ros/RosConnectionSymbol.java)). 

To keep the model clean of middleware specific code, the [Tagging](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/languages/Tagging) mechanism is used. Generate/Create a new Tagging Schema (Example: [RosToEmamTagSchema](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/languages/EmbeddedMontiArc/blob/master/src/main/java/de/monticore/lang/embeddedmontiarc/tagging/middleware/ros/RosToEmamTagSchema.java)) and SymbolCreator(Example: [RosConnectionSymbolCreator](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/languages/EmbeddedMontiArc/blob/master/src/main/java/de/monticore/lang/embeddedmontiarc/tagging/middleware/ros/RosConnectionSymbolCreator.java))
for your new MiddlewareSymbol. Make sure to load all your tags before starting generation(Example: [TagHelper::resolveTags](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/EMAM2RosCpp/blob/master/src/main/java/de/monticore/lang/monticar/generator/roscpp/helper/TagHelper.java))

## Generator
The new middleware generator needs to the following methods
* `setGenerationTargetPath(String path)`: Sets the directory the generated files are written to.
* `boolean willAccept(EMAComponentInstanceSymbol componentInstanceSymbol)`: Signals the DistributedTargetGenerator if the middleware generator will generate useful files for the given component.
    * Example: If a Component without RosConnections is passed to the ROS generator, it will reject it.
* `List<File> generate(EMAComponentInstanceSymbol componentInstanceSymbol, TaggingResolver taggingResolver)`: Generates all files(Adapter + CMake) for the given Component.

These 3 methods are combined in the Interface [GeneratorImpl](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/EMAM2Middleware/blob/master/src/main/java/de/monticore/lang/monticar/generator/middleware/impls/GeneratorImpl.java). 
To avoid a cyclic dependency between the new middleware generator and EMAM2Middleware, it is advised to write a small wrapper in EMAM2Middleware instead of implementing the Interface directly. See [RosCppGenImpl](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/EMAM2Middleware/blob/master/src/main/java/de/monticore/lang/monticar/generator/middleware/impls/RosCppGenImpl.java) for an example.


## Generated C++ Adapter
The generated C++ Adapter needs to stick to the following conventions:

All examples are given for the middleware ROS and a component named `AddComp` from the package `tests.a`.

* **File name:** {Middleware.name}Adapter_{Component.fullQualifiedName}.h
    * Example: `RosAdapter_tests_a_addComp.h`
* **Class name:** Java convention, file name = class name
    * Example: `class RosAdapter_tests_a_addComp` in `RosAdapter_tests_a_addComp.h`
* **Interfaces:** The generated coordinator for the Component contains the interface `IAdapter_{Component.fullQualifiedName}.h` which the new Adapter needs to implement.
    * Example: 
    
    ```
    #include "IAdapter_tests_a_addComp.h"
    ...
    class RosAdapter_tests_a_addComp: public IAdapter_tests_a_addComp{...};
    ```
* **init method:** The Interface contains the Method ``init({Component.fullQualifiedName}* comp)`` that sets the shared instance of the Component and starts the middleware in this thread.
    * Example:
   
    ```
    #include "tests_a_addComp.h"
    #include <ros/ros.h>
    ...
    tests_a_addComp* component;
    ...
    void init(tests_a_addComp* comp){
        this->component = comp; //saves the shared instance for later use
        ...
        ros::spin(); //starts ROS in this thread and blocks it!
    }
    ```
* **tick method:** The Interface contains the Method `tick()` that the coordinator executes in an defined interval(e.g. every 100ms). The middleware needs to publish all outgoing connections in this method. Do not call `component.execute()` here, as the coordinator aleady does that!
    * Example:
    
    ```
    void tick(){
        //publish the values of the out1 Port of the Component to ROS
        std_msgs::Float64 tmpMsg;
        tmpMsg.data = component->out1;
        _echoPublisher.publish(tmpMsg);
    }
    ```
* **Build script:** A CMake build script needs to be generated.
    1. The project needs to have the same name as the Adapter.
    2. The middleware libraries need to be a) located, b) linked, and c) included
    3. A target for a static library with the same name as the Adapter needs to be a) defined and b) exported
    4. The C++ code for the a) Component and b) Coordinator must be linked/included
    5. All includes and the header file for the adapter need to be accessible from the super project
    * Example:

    ```
    cmake_minimum_required(VERSION 3.5)
    #see i.
    project (RosAdapter_tests_a_addComp)
    
    #see ii.a)
    find_package(roscpp REQUIRED)
    find_package(rosgraph_msgs REQUIRED)
    find_package(std_msgs REQUIRED)
    
    #see iii.a)
    add_library(RosAdapter_tests_a_addComp RosAdapter_tests_a_addComp.h)
    #fix: .h file with C++ code instead of C code
    set_target_properties(RosAdapter_tests_a_addComp PROPERTIES LINKER_LANGUAGE CXX)
    #see ii.b) + iv.a)
    target_link_libraries(RosAdapter_tests_a_addComp tests_a_addComp ${roscpp_LIBRARIES} ${rosgraph_msgs_LIBRARIES} ${std_msgs_LIBRARIES})
    #see ii.c) + iv.b) + v. for PUBLIC keyword
    target_include_directories(RosAdapter_tests_a_addComp PUBLIC ${CMAKE_CURRENT_SOURCE_DIR} ${roscpp_INCLUDE_DIRS} ${rosgraph_msgs_INCLUDE_DIRS} ${std_msgs_INCLUDE_DIRS})
    #see iii.b)
    export(TARGETS RosAdapter_tests_a_addComp FILE RosAdapter_tests_a_addComp.cmake)
    ```   
