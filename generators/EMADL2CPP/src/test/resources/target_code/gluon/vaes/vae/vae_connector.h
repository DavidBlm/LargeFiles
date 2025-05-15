#ifndef VAE_CONNECTOR
#define VAE_CONNECTOR
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "vae_connector_decoder.h"
using namespace arma;
class vae_connector{
public:
colvec data;
cube res;
vae_connector_decoder decoder;
void init()
{
data=colvec(2);
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
