#run from project root!
# (c) https://github.com/MontiCore/monticore  
#make sure all ros packages have been sourced!
function quitOnError {
    if [ "$?" -ne 0 ]
    then
        echo Failed!
        exit 1
    fi
}

cd target/generated-sources-cmake/
quitOnError

for d in */ ; do
    echo Trying to build "$d"
    echo creating dir "$d"/build
    mkdir "$d"/build
    quitOnError
    echo CMake
    cmake -B"$d"/build/ -H"$d"/src/ -DCMAKE_BUILD_TYPE=DEBUG
    quitOnError
    echo make -j4
    make -C "$d"/build/
    quitOnError
done

echo Success!
exit 0
