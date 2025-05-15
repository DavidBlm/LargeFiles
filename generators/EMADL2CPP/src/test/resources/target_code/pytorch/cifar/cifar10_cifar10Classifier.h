#ifndef CIFAR10_CIFAR10CLASSIFIER
#define CIFAR10_CIFAR10CLASSIFIER
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "cifar10_cifar10Classifier_net.h"
#include "cifar10_cifar10Classifier_calculateClass.h"
using namespace arma;
class cifar10_cifar10Classifier{
public:
icube image;
int classIndex;
cifar10_cifar10Classifier_net net;
cifar10_cifar10Classifier_calculateClass calculateClass;
void init()
{
image = icube(3, 32, 32);
net.init();
calculateClass.init();
}
void execute()
{
net.data = image;
net.execute();
calculateClass.inputVector = net.softmax;
calculateClass.execute();
classIndex = calculateClass.maxIndex;
}

};
#endif
