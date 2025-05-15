#ifndef DEFAULTGAN_DEFAULTGANCONNECTOR
#define DEFAULTGAN_DEFAULTGANCONNECTOR
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "defaultGAN_defaultGANConnector_predictor.h"
using namespace arma;
class defaultGAN_defaultGANConnector{
public:
colvec noise;
cube res;
defaultGAN_defaultGANConnector_predictor predictor;
void init()
{
noise=colvec(100);
res = cube(1, 64, 64);
predictor.init();
}
void execute()
{
predictor.noise = noise;
predictor.execute();
res = predictor.data;
}

};
#endif
