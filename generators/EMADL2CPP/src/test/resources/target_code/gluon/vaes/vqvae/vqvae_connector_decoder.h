#ifndef VQVAE_CONNECTOR_DECODER
#define VQVAE_CONNECTOR_DECODER
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "CNNPredictor_vqvae_connector_decoder.h"
#include "CNNTranslator.h"
using namespace arma;
class vqvae_connector_decoder{
public:
CNNPredictor_vqvae_connector_decoder_0 _predictor_0_;
cube encoding;
cube data;
void init()
{
encoding = cube(16, 7, 7);
data = cube(1, 28, 28);
}
void execute(){
    vector<float> encoding_ = CNNTranslator::translate(encoding);

    vector<float> data_(1 * 28 * 28);


    _predictor_0_.predict(encoding_, data_);

    data = CNNTranslator::translateToCube(data_, std::vector<size_t> {1, 28, 28});

}

};
#endif
