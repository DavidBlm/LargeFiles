MXNET_PATH=$(python3 -c "import mxnet; print(mxnet.__file__)")
# (c) https://github.com/MontiCore/monticore  
MXNET_FOLDER=$(dirname $MXNET_PATH)
echo $MXNET_FOLDER

if [ ! -f $MXNET_FOLDER/libmxnet.so ]; then
    echo "libmxnet.so not found at default location" $MXNET_FOLDER
    echo "It should be there if the python mxnet package is installed"
    echo "Either fix the installation, or adapt the ./build.sh script to locate libmxnet.so correctly"
    exit 1
fi

rm -rf build
mkdir build && cd build

echo "Building DigitClassifier.."
cmake -D MXNET_PATH=$MXNET_FOLDER/libmxnet.so ..
make