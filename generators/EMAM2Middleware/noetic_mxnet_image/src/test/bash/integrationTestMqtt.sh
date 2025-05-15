#!/bin/bash
# (c) https://github.com/MontiCore/monticore  
set -e
baseDir=$(readlink -f `dirname $0`/../../..)

for f in `find $baseDir/target/generated-sources-mqtt/ -name compile.sh`
do
	bash -H $f
done
