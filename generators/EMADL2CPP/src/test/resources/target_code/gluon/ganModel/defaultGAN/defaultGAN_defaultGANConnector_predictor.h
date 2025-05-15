#ifndef DEFAULTGAN_DEFAULTGANCONNECTOR_PREDICTOR
#define DEFAULTGAN_DEFAULTGANCONNECTOR_PREDICTOR
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "CNNPredictor_defaultGAN_defaultGANConnector_predictor.h"
#include "CNNTranslator.h"
using namespace arma;
class defaultGAN_defaultGANConnector_predictor{
public:
CNNPredictor_defaultGAN_defaultGANConnector_predictor_0 _predictor_0_;
colvec noise;
cube data;
void init()
{
noise=colvec(100);
data = cube(1, 64, 64);
}
void execute(){
    vector<float> noise_ = CNNTranslator::translate(noise);

    vector<float> data_(1 * 64 * 64);


    _predictor_0_.predict(noise_, data_);

    data = CNNTranslator::translateToCube(data_, std::vector<size_t> {1, 64, 64});

}

};
#endif
