#ifndef CNNCALCULATOR_CONNECTOR_NUMBER2_TENS
#define CNNCALCULATOR_CONNECTOR_NUMBER2_TENS
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
using namespace arma;
class cNNCalculator_connector_number2_tens{
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
