<!-- (c) https://github.com/MontiCore/monticore -->
# Dependencies needed to compile the generated projects
## Note
The generator creates compile scripts for all supported compilers. A project with ROS or ROS2 contains `compile.sh` and `compileMsbuild.bat` as Linux and Windows(with Msbuild) are supported.
If you are having problems compiling on Windows because of the path length limit, use `substCompileMsbuild.bat` or  `substCompileMingw.bat`.
## All generated projects
CMake, Make, a C++ compiler, and Armadillo are required to compile the generated projects.
## Linux
Gcc is recommended as the C++ compiler.
Example install of all needed packages for ubuntu:
```bash
sudo apt install gcc cmake make
```
Then download Armadillo from here: [Linux](https://rwth-aachen.sciebo.de/s/igDWzLpdO5zYHBj/download?path=%2Fubuntu%2F18.10.24-armadillo-linux&files=armadillo-8.500.1-linux.zip) and set the environment variable `Armadillo_HOME` to the base dir of your installation.

To check everything is installed correctly use whereis/ls:
```bash
$ whereis g++
g++: /usr/bin/g++
$ whereis cmake
cmake: /usr/bin/cmake
$ whereis make
make: /usr/bin/make
$ ls "$Armadillo_HOME/include"
armadillo_bits armadillo.h
```

## Windows
Mingw64 gcc is recommended as the C++ compiler. Download a version with all needed tools from [here](https://rwth-aachen.sciebo.de/s/igDWzLpdO5zYHBj/download?path=%2Fwin64&files=mingw64.zip).

CMake for Windows: https://cmake.org/download/

Make for Windows: http://gnuwin32.sourceforge.net/packages/make.htm

Add the bin directories of all 3 to your PATH variable.

Then download Armadillo from here: [Windows](https://rwth-aachen.sciebo.de/s/igDWzLpdO5zYHBj/download?path=%2Fwin64&files=armadillo-8.200.2.zip) and set the environment variable `Armadillo_HOME` to the base dir of your installation.
To check everything is installed correctly use where/dir:
```batch
> where g++
C:\mingw64\bin\g++.exe
> where cmake
C:\Program Files\CMake\bin\cmake.exe
> where make
C:\Program Files\make-3.81-bin\bin\make.exe
> dir /b "%Armadillo_HOME%\include"
armadillo
armadillo.h
...
```

Alternatively Msbuild can be used as a compiler. Download and install Build Tools für Visual Studio 2017 by visiting [this](https://visualstudio.microsoft.com/de/downloads/) site, and navigating to `Tools for Visual Studio 2017`.
Set the environment variable `msbuild_HOME` to the Folder containing `vcvars64.bat`(Standard destination: "C:\Program Files (x86)\Microsoft Visual Studio\2017\BuildTools\VC\Auxiliary\Build")

To check everything is installed correctly use where/dir:
```batch
> where cmake
C:\Program Files\CMake\bin\cmake.exe
> dir /b "%Armadillo_HOME%\include"
armadillo
armadillo.h
...
> dir /b "%msbuild_HOME%"
vcvars32.bat
vcvars64.bat
...
> call "%msbuild_HOME%\vcvars64.bat"
**********************************************************************
** Visual Studio 2017 Developer Command Prompt v15.0
** Copyright (c) 2017 Microsoft Corporation
**********************************************************************
[vcvarsall.bat] Environment initialized for: 'x64'

> where msbuild
C:\Program Files (x86)\Microsoft Visual Studio\2017\BuildTools\MSBuild\15.0\Bin\MSBuild.exe
```


Please note: It is highly recommended, you stick to the exact versions as stated above. Otherwise you might run into trouble regarding the interplay between cmake/make and the Armadillo library. In particular problems have been reported using Cygwin.

## Projects with roscpp generator
Only for generated projects that contain a ROS adapter(e.g. "generators":["cpp","roscpp"]).
ROS Kinetic currently only supports Linux and the installation is described [here](http://wiki.ros.org/kinetic/Installation/Ubuntu).
ROS Melodic is also supported on Windows and the installation is described [here](http://wiki.ros.org/Installation/Windows). You might need to patch the file `${ROS_HOME}\ros\console.h` like this:
```C++
extern ROSCONSOLE_DECL bool g_initialized;
=>
//extern ROSCONSOLE_DECL bool g_initialized;
...
if (ROS_UNLIKELY(!::ros::console::g_initialized)) \
=>
/*if (ROS_UNLIKELY(!::ros::console::g_initialized))*/ \
```


Set the environment varialble `ROS_HOME` to the base of your ROS installation.

## Projects with ros2cpp/rclcpp generator
Only for generated projects that contain a ROS2 adapter(e.g. "generators":["cpp","rclcpp"]).
Tested under ROS2 Bouncy and Crystal with Windows 10 and Ubuntu 18.04 respectively and the installation is described [here](https://index.ros.org/doc/ros2/Installation/).
ROS2 under Windows can only be compiled with msbuild.
Set the environment varialble `ROS2_HOME` to the base of your ROS2 installation.
If you use structs and message generation, you need to install colcon:
```bash
pip3 install -U colcon-common-extensions
```
