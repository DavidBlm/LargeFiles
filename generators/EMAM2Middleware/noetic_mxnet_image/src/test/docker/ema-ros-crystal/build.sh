#!/usr/bin/env bash
# (c) https://github.com/MontiCore/monticore  
curDir=$(readlink -f `dirname $0`)
docker build -t registry.git.rwth-aachen.de/monticore/embeddedmontiarc/generators/emam2middleware/ema-ros-crystal $curDir
