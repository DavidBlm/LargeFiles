#!/usr/bin/env bash
# (c) https://github.com/MontiCore/monticore  
set -e
export CMAKE_PREFIX_PATH=$AMENT_PREFIX_PATH:$CMAKE_PREFIX_PATH


# add *_HOME to PATH temporarily
if [ -n "$cmake_HOME" ]
then
    export PATH="$cmake_HOME:$PATH"
fi
if [ -n "$make_HOME" ]
then
    export PATH="$make_HOME:$PATH"
fi

# check if needed programs are in PATH
if [[ `command -v cmake` ]]
then
    echo "Found cmake"
else
    echo "Can not find cmake in PATH! Aborting."
    echo "Try setting the environment variable cmake_HOME to the base of your installation or adding it to your PATH!"
    exit 1
fi
if [[ `command -v make` ]]
then
    echo "Found make"
else
    echo "Can not find make in PATH! Aborting."
    echo "Try setting the environment variable make_HOME to the base of your installation or adding it to your PATH!"
    exit 1
fi

# source additional environment variables
source "$ROS_HOME"/setup.bash

# Post source check if needed programs are in PATH
if [[ `command -v roscore` ]]
then
    echo "Found roscore"
else
    echo "Can not find roscore in PATH! Aborting."
    echo "Try setting the environment variable ROS_HOME to the base of your installation or adding it to your PATH!"
    exit 1
fi

curDir=`dirname "$0"`
# configure cmake
cmake -B"$curDir/build/" -H"$curDir/src/" -DCMAKE_INSTALL_PREFIX="$curDir/install"  "$@"
# build
cmake --build "$curDir/build/" --target install --config Release
