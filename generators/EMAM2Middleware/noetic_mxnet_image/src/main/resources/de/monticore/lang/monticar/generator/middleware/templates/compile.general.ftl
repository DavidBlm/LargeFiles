<#-- (c) https://github.com/MontiCore/monticore -->
<#macro cmake subdir="">
<@comment str="configure cmake"/>
cmake -B<@evalCurDir postfix="build/${subdir}"/> -H<@evalCurDir postfix="src/${subdir}"/> -DCMAKE_INSTALL_PREFIX=<@evalCurDir postfix="install"/> <@generatorOption/> <@passParams/>
<@comment str="build"/>
cmake --build <@evalCurDir postfix="build/${subdir}"/> --target install --config Release
</#macro>

<@comment str="add *_HOME to PATH temporarily"/>
<#list model.getAdditionalPathDirs() as new_exe>
    <@addToPath new_exe=new_exe/>
</#list>

<@comment str="check if needed programs are in PATH"/>
<#list model.getExecutables() as exe>
    <@exeCheck exe=exe/>
</#list>

<@comment str="source additional environment variables"/>
<#list model.getEnvironmentFiles() as env_file>
    <@source env_file=env_file/>
</#list>

<@comment str="Post source check if needed programs are in PATH"/>
<#list model.getPostSourceExecutables() as exe>
    <@exeCheck exe=exe/>
</#list>

<@setCurDir/>
<#if model.useRos2() && model.useStructMsgs()>
pushd .
cd <@evalCurDir/>
colcon build
popd
    <@localSource env_file="install/local_setup"/>

    <@cmake subdir="comps"/>
<#else>
    <@cmake/>
</#if>
