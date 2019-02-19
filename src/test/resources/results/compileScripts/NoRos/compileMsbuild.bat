@ECHO Off
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
   exit /B 1
)
where vcvars64.bat
IF NOT %ERRORLEVEL% EQU 0 (
   echo "Can not find vcvars64.bat in PATH! Aborting."
   exit /B 1
)

:: source additional environment variables
call vcvars64.bat

:: Post source check if needed programs are in PATH
where msbuild
IF NOT %ERRORLEVEL% EQU 0 (
   echo "Can not find msbuild in PATH! Aborting."
   exit /B 1
)

:: cmake
cmake -B./build/ -G "Visual Studio 15 2017 Win64" %* ./src

:: msbuild
cd .\build
msbuild /m /t:build /p:Configuration=Release ALL_BUILD.vcxproj
cd ..