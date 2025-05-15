<#-- (c) https://github.com/MontiCore/monticore -->
#!/usr/bin/env bash
set -e
export CMAKE_PREFIX_PATH=$AMENT_PREFIX_PATH:$CMAKE_PREFIX_PATH
<#-- General bash -->
<#macro setCurDir>
curDir=`dirname "$0"`
</#macro>
<#macro evalCurDir postfix="">"$curDir/${postfix}"</#macro>
<#macro passParams>"$@"</#macro>
<#macro source env_file>
source ${env_file}
</#macro>
<#macro localSource env_file>
source "$curDir/${env_file}.bash"
</#macro>

<#macro comment str>
# ${str}
</#macro>
<#-- Compile specific -->
<#macro addToPath new_exe>
if [ -n "$${new_exe}_HOME" ]
then
    export PATH="$${new_exe}_HOME:$PATH"
fi
</#macro>
<#macro exeCheck exe>
if [[ `command -v ${exe}` ]]
then
    echo "Found ${exe}"
else
    echo "Can not find ${exe} in PATH! Aborting."
    echo "${model.getAdditionalErrorMsg(exe)}"
    exit 1
fi
</#macro>
<#macro generatorOption></#macro>

<#include "compile.general.ftl"/>
