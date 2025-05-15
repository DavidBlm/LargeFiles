#!/bin/bash

rm -rf target
rm -rf model

java -jar embedded-montiarc-emadl-generator-0.5.13-SNAPSHOT-jar-with-dependencies.jar -m src/main/emadl/ -r calculator.Connector -o target -b GLUON -c y -f y -ad -dn "Network"

FILE=model/calculator.Network.Net1_decomposed/model_Net1_decomposed-0000.params
if [ ! -f "$FILE" ]; then
    echo "$FILE does not exist."; exit 1;
fi

FILE=model/calculator.Network.Net1_decomposed/model_Net1_decomposed-old-symbol.json
if [ ! -f "$FILE" ]; then
    echo "$FILE does not exist."; exit 1;
fi

FILE=model/calculator.Network.Net1_decomposed/model_Net1_decomposed-symbol.json
if [ ! -f "$FILE" ]; then
    echo "$FILE does not exist."; exit 1;
fi

FILE=model/calculator.Network.Net2_decomposed/model_Net2_decomposed-0000.params
if [ ! -f "$FILE" ]; then
    echo "$FILE does not exist."; exit 1;
fi

FILE=model/calculator.Network.Net2_decomposed/model_Net2_decomposed-old-symbol.json
if [ ! -f "$FILE" ]; then
    echo "$FILE does not exist."; exit 1;
fi

FILE=model/calculator.Network.Net2_decomposed/model_Net2_decomposed-symbol.json
if [ ! -f "$FILE" ]; then
    echo "$FILE does not exist."; exit 1;
fi

echo "All files found. Network successfully decomposed."


