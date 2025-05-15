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

# cd ./gluon-pretrained
# rm -rf ./target
# rm -rf ./model
# java -jar ../embedded-montiarc-emadl-generator-0.5.5-SNAPSHOT-jar-with-dependencies.jar -m ./src/emadl/models/ -r cNNCalculator.Connector -o target -b GLUON -p /usr/bin/python3

# cp ./model/cNNCalculator.Network/model_0_newest.onnx ../model_0-0000.onnx

cd ./tensorflow-pretrained
rm -rf ./target
rm -rf ./model
java -jar ../embedded-montiarc-emadl-generator-0.5.5-SNAPSHOT-jar-with-dependencies.jar -m ./src/emadl/models/ -r cNNCalculator.Connector -o target -b TENSORFLOW -c n -p /usr/bin/python3

cp ./model/cNNCalculator.Network/model.onnx ../model_0-0000.onnx

cd ../gluon-load
rm -rf ./target
rm -rf ./model
java -jar ../embedded-montiarc-emadl-generator-0.5.5-SNAPSHOT-jar-with-dependencies.jar -m ./src/emadl/models/ -r cNNCalculator.Connector -o target -b GLUON -p /usr/bin/python3

rm -rf build
mkdir build && cd build

echo "Building DigitClassifier.."
cmake -D MXNET_PATH=$MXNET_FOLDER/libmxnet.so ..
make