@rem (c) https://github.com/MontiCore/monticore  

@echo off

:: add *_HOME to PATH temporarily
IF NOT [%cmake_HOME%] == [] (
	set PATH="%cmake_HOME%;%PATH%"
)
IF NOT [%make_HOME%] == [] (
	set PATH="%make_HOME%;%PATH%"
)
IF NOT [%g++_HOME%] == [] (
	set PATH="%g++_HOME%;%PATH%"
)

:: check if needed programs are in PATH
where cmake
IF NOT %ERRORLEVEL% EQU 0 (
	echo "Can not find cmake in PATH! Aborting."
    echo "Try setting the environment variable cmake_HOME to the base of your installation or adding it to your PATH!"
	exit /B 1
)
where make
IF NOT %ERRORLEVEL% EQU 0 (
	echo "Can not find make in PATH! Aborting."
    echo "Try setting the environment variable make_HOME to the base of your installation or adding it to your PATH!"
	exit /B 1
)
where g++
IF NOT %ERRORLEVEL% EQU 0 (
	echo "Can not find g++ in PATH! Aborting."
    echo "Try setting the environment variable g++_HOME to the base of your installation or adding it to your PATH!"
	exit /B 1
)

:: source additional environment variables

:: Post source check if needed programs are in PATH

SET curDir=%~dp0
:: configure cmake
cmake -B%curDir%/build/ -H%curDir%/src/ -DCMAKE_INSTALL_PREFIX=%curDir%/install -G "MinGW MakeFiles" %*
:: build
cmake --build %curDir%/build/ --target install --config Release
