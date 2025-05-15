#ifndef CNNCALCULATOR_CONNECTOR_ADD
#define CNNCALCULATOR_CONNECTOR_ADD
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
using namespace arma;
class cNNCalculator_connector_add{
public:
int num1;
int num2;
int sum;
void init()
{
}
void execute()
{
sum = num1+num2;
}

};
#endif
