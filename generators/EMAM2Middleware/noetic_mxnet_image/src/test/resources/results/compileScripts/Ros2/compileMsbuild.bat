@rem (c) https://github.com/MontiCore/monticore  

@echo off

:: add *_HOME to PATH temporarily
IF NOT [%cmake_HOME%] == [] (
	set PATH="%cmake_HOME%;%PATH%"
)
IF NOT [%msbuild_HOME%] == [] (
	set PATH="%msbuild_HOME%;%PATH%"
)

:: check if needed programs are in PATH
where cmake
IF NOT %ERRORLEVEL% EQU 0 (
	echo "Can not find cmake in PATH! Aborting."
    echo "Try setting the environment variable cmake_HOME to the base of your installation or adding it to your PATH!"
	exit /B 1
)
where vcvars64.bat
IF NOT %ERRORLEVEL% EQU 0 (
	echo "Can not find vcvars64.bat in PATH! Aborting."
    echo "Try setting the environment variable msbuild_HOME to the base of your installation or adding it to your PATH!"
	exit /B 1
)

:: source additional environment variables
call vcvars64.bat
call %ROS2_HOME%\local_setup.bat

:: Post source check if needed programs are in PATH
where msbuild
IF NOT %ERRORLEVEL% EQU 0 (
	echo "Can not find msbuild in PATH! Aborting."
    echo "Try setting the environment variable msbuild_HOME to the base of your installation or adding it to your PATH!"
	exit /B 1
)
where ros2
IF NOT %ERRORLEVEL% EQU 0 (
	echo "Can not find ros2 in PATH! Aborting."
    echo "Try setting the environment variable ROS2_HOME to the base of your installation or adding it to your PATH!"
	exit /B 1
)

SET curDir=%~dp0
:: configure cmake
cmake -B%curDir%/build/ -H%curDir%/src/ -DCMAKE_INSTALL_PREFIX=%curDir%/install -G "Visual Studio 15 2017 Win64" %*
:: build
cmake --build %curDir%/build/ --target install --config Release
