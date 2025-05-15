#ifndef CNNCALCULATOR_CONNECTOR_NUMBER2
#define CNNCALCULATOR_CONNECTOR_NUMBER2
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
using namespace arma;
class cNNCalculator_connector_number2{
public:
int hundreds;
int tens;
int ones;
int number;
void init()
{
}
void execute()
{
number = ones+10*tens+100*hundreds;
}

};
#endif
