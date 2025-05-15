/* (c) https://github.com/MontiCore/monticore */
#ifndef MNIST_MNISTCLASSIFIER_NET
#define MNIST_MNISTCLASSIFIER_NET
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "CNNPredictor_mnist_mnistClassifier_net.h"
#include "CNNTranslator.h"
using namespace arma;
class mnist_mnistClassifier_net{
const int classes = 10;
public:
CNNPredictor_mnist_mnistClassifier_net_0 _predictor_0_;
icube image;
colvec predictions;
void init()
{
image = icube(1, 28, 28);
predictions=colvec(classes);
}
void execute(){
    vector<float> image_ = CNNTranslator::translate(image);

    vector<float> predictions_(10);


    _predictor_0_.predict(image_, predictions_);

    predictions = CNNTranslator::translateToCol(predictions_, std::vector<size_t> {10});

}

};
#endif
