<!-- (c) https://github.com/MontiCore/monticore -->
# Quickstart guide for generator users
- Download the latest version of the generator from the [se-nexus](https://nexus.se.rwth-aachen.de/service/rest/repository/browse/public/de/monticore/lang/monticar/embedded-montiarc-math-middleware-generator/) (e.g. .../0.0.20-20190311.154342-1/embedded-montiarc-math-middleware-generator-0.0.20-20190311.154342-1-jar-with-dependencies.jar) and save it as mw-generator.jar
- Create a `project.json` config file for your project
    - make sure your model directory and root model are correct
    - for examples see: [valid.json](src/test/resources/config/valid.json), [emadl.json](src/test/resources/config/emadl.json)
    - Refer to the main [readme.md](README.md)(section CLI) to see all options.
- Generate your project by running  
```bash
java -jar mw-generator.jar project.json
```

- Refer to the main [readme.md](README.md)(Section: Dependencies needed to compile the generated projects) for instructions on how to install the needed dependencies for compilation

- Compile your project by running one of the generated compile scripts. E.g.:
```bash
cd defined/output/directory
./compile.sh
# or on Windows
call compileMingw.bat
# or
call compileMsbuild.bat
# or to avoid path length error:
call substCompileMingw.bat
```
    
For two example Projects using this generator see:
- [Cooperative Intersection](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/applications/cooperativeintersection) (Uses EMAM, ROS, CoInCar Simulator)
- [Autonomous driving](https://git.rwth-aachen.de/autonomousdriving/torcs_dl) (Uses EMADL, ROS, Torcs Simulator)
