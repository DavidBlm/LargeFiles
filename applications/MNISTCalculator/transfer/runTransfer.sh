#!/bin/bash

rm -rf target
rm -rf model

## Run the composed model trained on the MNIST dataset
mvn dependency:resolve emadl:train -f pom_digits.xml -s settings.xml

if [ $? -ne 0 ]; then
    echo "Execution of EMADL-Generator for the digit base model failed."
    exit 1
fi

PARAMSFILE=model/digits.DigitsComposed.DigitsInputLayer_decomposed/model_DigitsInputLayer_decomposed-0000.params
SYMBOLFILE=model/digits.DigitsComposed.DigitsInputLayer_decomposed/model_DigitsInputLayer_decomposed-old-symbol.json

if [ ! -f "$PARAMSFILE" ] || [ ! -f "$SYMBOLFILE" ]; then
    echo "Required model files do not exist:"
    [ ! -f "$PARAMSFILE" ] && echo "$PARAMSFILE not found."
    [ ! -f "$SYMBOLFILE" ] && echo "$SYMBOLFILE not found."
    exit 1
fi

rm -rf target

# Run the "partially" pretrained model trained on the EMNIST dataset
mvn dependency:resolve emadl:train -f pom_letters.xml -s settings.xml