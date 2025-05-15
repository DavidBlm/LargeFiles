<#-- (c) https://github.com/MontiCore/monticore -->
<#-- General batch -->
<#macro comment str>
:: ${str}
</#macro>
<#macro setCurDir>
SET curDir=%~dp0
</#macro>
<#macro evalCurDir postfix="">%curDir%/${postfix}</#macro>
<#macro passParams>%*</#macro>
<#macro source env_file>
call ${env_file}
</#macro>
<#macro localSource env_file>
call "%curDir%/${env_file}.bat"
</#macro>
<#-- Compile specific -->
<#macro addToPath new_exe>
IF NOT [%${new_exe}_HOME%] == [] (
	set PATH="%${new_exe}_HOME%;%PATH%"
)
</#macro>
<#macro exeCheck exe>
where ${exe}
IF NOT %ERRORLEVEL% EQU 0 (
	echo "Can not find ${exe} in PATH! Aborting."
    echo "${model.getAdditionalErrorMsg(exe)}"
	exit /B 1
)
</#macro>
<#if model.getKind() == "MINGW">
<#macro generatorOption>-G "MinGW MakeFiles"</#macro>
<#else>
<#macro generatorOption>-G "Visual Studio 15 2017 Win64"</#macro>
</#if>

@echo off
<#include "compile.general.ftl"/>
