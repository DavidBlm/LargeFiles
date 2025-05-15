#ifndef INFOGAN_INFOGANCONNECTOR_PREDICTOR
#define INFOGAN_INFOGANCONNECTOR_PREDICTOR
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "CNNPredictor_infoGAN_infoGANConnector_predictor.h"
#include "CNNTranslator.h"
using namespace arma;
class infoGAN_infoGANConnector_predictor{
public:
CNNPredictor_infoGAN_infoGANConnector_predictor_0 _predictor_0_;
colvec noise;
ivec c1;
cube data;
void init()
{
noise=colvec(62);
c1=ivec(10);
data = cube(1, 64, 64);
}
void execute(){
    vector<float> noise_ = CNNTranslator::translate(noise);
    vector<float> c1_ = CNNTranslator::translate(c1);

    vector<float> data_(1 * 64 * 64);


    _predictor_0_.predict(noise_, c1_, data_);

    data = CNNTranslator::translateToCube(data_, std::vector<size_t> {1, 64, 64});

}

};
#endif
