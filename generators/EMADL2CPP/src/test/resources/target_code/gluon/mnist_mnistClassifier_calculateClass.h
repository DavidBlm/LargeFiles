/* (c) https://github.com/MontiCore/monticore */
#ifndef MNIST_MNISTCLASSIFIER_CALCULATECLASS
#define MNIST_MNISTCLASSIFIER_CALCULATECLASS
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
using namespace arma;
class mnist_mnistClassifier_calculateClass{
const int n = 10;
public:
colvec inputVector;
int maxIndex;
double maxValue;
void init()
{
inputVector=colvec(n);
}
void execute()
{
maxIndex = 0;
maxValue = inputVector(1-1);
for( auto i=2;i<=n;++i){
if((inputVector(i-1) > maxValue)){
maxIndex = i-1;
maxValue = inputVector(i-1);
}
}
}

};
#endif
