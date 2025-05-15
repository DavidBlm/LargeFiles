#ifndef CVAE_CONNECTOR
#define CVAE_CONNECTOR
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "cvae_connector_decoder.h"
using namespace arma;
class cvae_connector{
public:
colvec encoding;
ivec label;
cube res;
cvae_connector_decoder decoder;
void init()
{
encoding=colvec(2);
label=ivec(10);
res = cube(1, 28, 28);
decoder.init();
}
void execute()
{
decoder.encoding = encoding;
decoder.label = label;
decoder.execute();
res = decoder.data;
}

};
#endif
