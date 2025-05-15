/* (c) https://github.com/MontiCore/monticore */
#ifndef MNIST_MNISTCLASSIFIER
#define MNIST_MNISTCLASSIFIER
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "mnist_mnistClassifier_net.h"
#include "mnist_mnistClassifier_calculateClass.h"
using namespace arma;
class mnist_mnistClassifier{
public:
icube image;
int classIndex;
double probability;
mnist_mnistClassifier_net net;
mnist_mnistClassifier_calculateClass calculateClass;
void init()
{
image = icube(1, 28, 28);
net.init();
calculateClass.init();
}
void execute()
{
net.image = image;
net.execute();
calculateClass.inputVector = net.predictions;
calculateClass.execute();
classIndex = calculateClass.maxIndex;
probability = calculateClass.maxValue;
}

};
#endif
