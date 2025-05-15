#!/bin/bash
# (c) https://github.com/MontiCore/monticore  

cd target/generated-sources-cmake/lab/
rm -rf build

mkdir build
cd build

cmake ../src
make -j4
