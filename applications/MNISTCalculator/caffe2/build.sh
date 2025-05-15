# (c) https://github.com/MontiCore/monticore  
rm -rf target

java -jar ../embedded-montiarc-emadl-generator-0.4.1-jar-with-dependencies.jar -m src/emadl/models/ -r cNNCalculator.Connector -o target -b CAFFE2

rm -rf build
mkdir build && cd build

echo "Building DigitClassifier.."
cmake ..
#cp -r /home/christopher/Documents/Master/Semester2/Praktikum/mxnet/include/mxnet/ ../target/
make
