#ifndef LOADNETWORKMNIST
#define LOADNETWORKMNIST
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "CNNPredictor_loadNetworkMnist.h"
#include "CNNTranslator.h"
using namespace arma;
class loadNetworkMnist{
public:
CNNPredictor_loadNetworkMnist_0 _predictor_0_;
icube image;
colvec predictions;
void init()
{
image = icube(1, 28, 28);
predictions=colvec(10);
}
void execute(){
    vector<float> CNN_predictions_(10);

    _predictor_0_.predict(CNNTranslator::translate(image),
                CNN_predictions_);

    predictions = CNNTranslator::translateToCol(CNN_predictions_, std::vector<size_t> {10});

}

};
#endif
