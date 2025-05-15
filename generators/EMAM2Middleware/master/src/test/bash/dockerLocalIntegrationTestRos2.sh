#!/usr/bin/env bash
# (c) https://github.com/MontiCore/monticore  
baseDir=$(readlink -f `dirname $0`/../../..)
docker run -t -v $baseDir:/project registry.git.rwth-aachen.de/monticore/embeddedmontiarc/generators/emam2middleware/ema-ros-crystal /bin/bash -c "cd project; src/test/bash/integrationTestRos2.sh"
chown -R `who am i | awk '{print $1}'` "$baseDir/target/generated-sources-ros2/"
