<#-- (c) https://github.com/MontiCore/monticore -->
@echo off
:: Substituting the current dir with a drive letter to bypass windows path length limit
:: Set environment variable EMA_SUBST_LETTER to change letter is used
:: Default is N

IF NOT [%EMA_SUBST_LETTER%] == [] (
    set usedLetter=%EMA_SUBST_LETTER%
) else (
    set usedLetter=N
)

set curDir=%~dp0
set curDir=%curDir:~0,-1%

subst /d %usedLetter%:
subst %usedLetter%: "%curDir%"
pushd .

cd /d %usedLetter%:
call ${model.getFileName()}

popd
subst /d %usedLetter%:
