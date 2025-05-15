/* (c) https://github.com/MontiCore/monticore */
#ifndef CIFAR10_CIFAR10CLASSIFIER_NET
#define CIFAR10_CIFAR10CLASSIFIER_NET
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "CNNPredictor_cifar10_cifar10Classifier_net.h"
#include "CNNTranslator.h"
using namespace arma;
class cifar10_cifar10Classifier_net{
const int classes = 10;
public:
CNNPredictor_cifar10_cifar10Classifier_net_0 _predictor_0_;
icube data;
colvec softmax;
void init()
{
data = icube(3, 32, 32);
softmax=colvec(classes);
}
void execute(){
    vector<float> CNN_softmax_(10);

    _predictor_0_.predict(CNNTranslator::translate(data),
                CNN_softmax_);

    softmax = CNNTranslator::translateToCol(CNN_softmax_, std::vector<size_t> {10});

}

};
#endif
