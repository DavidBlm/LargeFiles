#!/usr/bin/env bash
# (c) https://github.com/MontiCore/monticore  
#run from project root!
#make sure all ros packages have been sourced!
function quitOnError {
    if [ "$?" -ne 0 ]
    then
        echo "Failed building $1!"
        echo "$1" >> failed.log
        exit 1
    fi
}

function build() {
    echo Trying to build "$1"
    echo creating dir "$1"/build
    mkdir "$1"/build
    # quitOnError "$1"
    if [[ `command -v ccache` ]]
    then
        echo CMake with ccache
        export CXX="/usr/bin/g++"
        cmake -B"$1"/build/ -H"$1"/src/ -DCMAKE_BUILD_TYPE=DEBUG -DCMAKE_CXX_COMPILER_LAUNCHER="ccache"
    else
        echo CMake without ccache
        cmake -B"$1"/build/ -H"$1"/src/ -DCMAKE_BUILD_TYPE=DEBUG
    fi

    quitOnError "$1"
    echo make
    make -s -C "$1"/build/
    quitOnError "$1"
}

cd target/generated-sources-cmake/
quitOnError "cd"
rm "failed.log"

export -f quitOnError
export -f build

projects=`find -maxdepth 1 -type d -not -name .`
parallel --no-notice -N1 --halt soon,fail=1 --eta -m build ::: $projects
if [[ -f "failed.log" ]]
then
    echo "Failed!"
    exit 1
else
    echo "Success!"
    exit 0
fi
