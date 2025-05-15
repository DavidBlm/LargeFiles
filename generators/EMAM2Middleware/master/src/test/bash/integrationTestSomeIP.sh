#!/bin/bash
# (c) https://github.com/MontiCore/monticore  
set -e
baseDir=$(readlink -f `dirname $0`/../../..)

for f in `find $baseDir/target/generated-sources-someip/ -name compile.sh`
do
	bash -H $f
done
