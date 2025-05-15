/* (c) https://github.com/MontiCore/monticore */
#ifndef ADD
#define ADD
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
using namespace arma;
class add{
public:
double a;
double b;
double c;
void init()
{
}
void execute()
{
c = a+b;
}

};
#endif
