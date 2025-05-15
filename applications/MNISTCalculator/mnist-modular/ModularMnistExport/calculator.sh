#!/bin/bash
set -e
echo "Found the following files: $(ls input | tr '\n' ' ')"
if [ $(ls input | wc -l) == "6" ]; then
    echo "Found exactly 6 input files. The mnistcalculator will run with the input files.";
    python calculator.py --image_input "$(ls input | sed 's/.*/input\/&/g' | tr '\n' ' ')";
else
    echo "Found more or less than 6 input files. The mnistcalculator will run with random inputs from the testing dataset.";
    python calculator.py;
fi

tail -f /dev/null