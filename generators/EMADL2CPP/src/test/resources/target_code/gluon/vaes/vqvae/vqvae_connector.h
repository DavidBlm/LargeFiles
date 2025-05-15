#ifndef VQVAE_CONNECTOR
#define VQVAE_CONNECTOR
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "vqvae_connector_decoder.h"
using namespace arma;
class vqvae_connector{
public:
cube data;
cube res;
vqvae_connector_decoder decoder;
void init()
{
data = cube(16, 7, 7);
res = cube(1, 28, 28);
decoder.init();
}
void execute()
{
decoder.encoding = data;
decoder.execute();
res = decoder.data;
}

};
#endif
