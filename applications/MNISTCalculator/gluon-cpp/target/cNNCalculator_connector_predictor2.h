#ifndef CNNCALCULATOR_CONNECTOR_PREDICTOR2
#define CNNCALCULATOR_CONNECTOR_PREDICTOR2
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "CNNPredictor_cNNCalculator_connector_predictor2.h"
#include "CNNTranslator.h"
using namespace arma;
class cNNCalculator_connector_predictor2{
const int classes = 10;
public:
CNNPredictor_cNNCalculator_connector_predictor2_0 _predictor_0_;
icube data;
colvec softmax;
void init()
{
data = icube(1, 28, 28);
softmax=colvec(classes);
}
void execute(){
    vector<float> data_ = CNNTranslator::translate(data);

    vector<float> softmax_(10);


    _predictor_0_.predict(data_, softmax_);

    softmax = CNNTranslator::translateToCol(softmax_, std::vector<size_t> {10});

}

};
#endif
