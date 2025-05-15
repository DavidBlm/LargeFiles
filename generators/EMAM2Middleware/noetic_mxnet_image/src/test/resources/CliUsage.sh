#!/usr/bin/env bash
# (c) https://github.com/MontiCore/monticore  
# via config file
java -jar target/embedded-montiarc-math-middleware-generator-0.0.13-SNAPSHOT-jar-with-dependencies.jar src/test/resources/config/valid.json
# via raw json string
java -jar target/embedded-montiarc-math-middleware-generator-0.0.13-SNAPSHOT-jar-with-dependencies.jar -r "{'modelsDir': 'src/test/resources/','outputDir': 'target/cliTest/validConfigFile','rootModel':'tests.a.addComp','generators': ['cpp','roscpp']}"
