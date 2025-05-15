<!-- (c) https://github.com/MontiCore/monticore -->
# MNISTCalculator Caffe2


## Prerequisites
1. Ubuntu Linux 16.04 LTS or 18.04 LTS (experimental)
2. Deep Learning Framework **Caffe2**. [Follow the instructions from this link](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/CNNArch2Caffe2#ubuntu).
3. Armadillo (at least armadillo version 6.600 must be used) [Official instructions at Armadillo Website](http://arma.sourceforge.net/download.html).
4. OpenCV


## How to Run
Generate and build the code for the EMADL model by executing:

```
bash build.sh
```

Finally, run the calculator as follows:
```
./build/src/cpp/DigitCalculator resources/images/2.jpg resources/images/3.jpg resources/images/4.png resources/images/6.png resources/images/2.jpg resources/images/3.jpg
```

You can try different images from the provided `/test_img` directory or even create your own images for testing. Note that the application program DigitCalculator will automatically rescale the given input image to the size 28x28.


## Troubleshooting Help

ERROR: HelperA.h:79:28: error: ‘sqrtmat’ was not declared in this scope.

FIX:
Copy compiled armadillo lib and include files to usr/lib and usr/include respectively. Replace YOUR_ARMADILLO_REPOSITORY and VERSION (e.g. 8.500.1) with your corresponding information:
```
cd YOUR_ARMADILLO_REPOSITORY/armadillo-VERSION
sudo cp libarmadillo* /usr/lib
cd YOUR_ARMADILLO_REPOSITORY/armadillo-VERSION/include
sudo cp -r * /usr/include
