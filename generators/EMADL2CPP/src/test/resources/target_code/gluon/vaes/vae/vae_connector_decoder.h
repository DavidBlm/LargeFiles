#ifndef VAE_CONNECTOR_DECODER
#define VAE_CONNECTOR_DECODER
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "CNNPredictor_vae_connector_decoder.h"
#include "CNNTranslator.h"
using namespace arma;
class vae_connector_decoder{
public:
CNNPredictor_vae_connector_decoder_0 _predictor_0_;
colvec encoding;
cube data;
void init()
{
encoding=colvec(2);
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
