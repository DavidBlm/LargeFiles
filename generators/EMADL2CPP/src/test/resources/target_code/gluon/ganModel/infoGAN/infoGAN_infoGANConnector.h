#ifndef INFOGAN_INFOGANCONNECTOR
#define INFOGAN_INFOGANCONNECTOR
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "infoGAN_infoGANConnector_predictor.h"
using namespace arma;
class infoGAN_infoGANConnector{
public:
colvec noise;
ivec c1;
cube res;
infoGAN_infoGANConnector_predictor predictor;
void init()
{
noise=colvec(62);
c1=ivec(10);
res = cube(1, 64, 64);
predictor.init();
}
void execute()
{
predictor.noise = noise;
predictor.c1 = c1;
predictor.execute();
res = predictor.data;
}

};
#endif
