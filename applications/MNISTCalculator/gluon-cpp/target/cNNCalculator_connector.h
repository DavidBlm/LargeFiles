#ifndef CNNCALCULATOR_CONNECTOR
#define CNNCALCULATOR_CONNECTOR
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "cNNCalculator_connector_predictor1.h"
#include "cNNCalculator_connector_predictor2.h"
#include "cNNCalculator_connector_predictor3.h"
#include "cNNCalculator_connector_predictor4.h"
#include "cNNCalculator_connector_predictor5.h"
#include "cNNCalculator_connector_predictor6.h"
#include "cNNCalculator_connector_cal.h"
using namespace arma;
class cNNCalculator_connector{
public:
icube image1;
icube image2;
icube image3;
icube image4;
icube image5;
icube image6;
int res;
cNNCalculator_connector_predictor1 predictor1;
cNNCalculator_connector_predictor2 predictor2;
cNNCalculator_connector_predictor3 predictor3;
cNNCalculator_connector_predictor4 predictor4;
cNNCalculator_connector_predictor5 predictor5;
cNNCalculator_connector_predictor6 predictor6;
cNNCalculator_connector_cal cal;
void init()
{
image1 = icube(1, 28, 28);
image2 = icube(1, 28, 28);
image3 = icube(1, 28, 28);
image4 = icube(1, 28, 28);
image5 = icube(1, 28, 28);
image6 = icube(1, 28, 28);
predictor1.init();
predictor2.init();
predictor3.init();
predictor4.init();
predictor5.init();
predictor6.init();
cal.init();
}
void execute()
{
predictor1.data = image1;
predictor2.data = image2;
predictor3.data = image3;
predictor4.data = image4;
predictor5.data = image5;
predictor6.data = image6;
predictor1.execute();
cal.number1_hundreds.inputVector = predictor1.softmax;
predictor2.execute();
cal.number1_tens.inputVector = predictor2.softmax;
predictor3.execute();
cal.number1_ones.inputVector = predictor3.softmax;
predictor4.execute();
cal.number2_hundreds.inputVector = predictor4.softmax;
predictor5.execute();
cal.number2_tens.inputVector = predictor5.softmax;
predictor6.execute();
cal.number2_ones.inputVector = predictor6.softmax;
cal.number1.execute();
cal.number1.ones = cal.number1_ones.maxIndex;
cal.number1.execute();
cal.number1.tens = cal.number1_tens.maxIndex;
cal.number1.execute();
cal.number1.hundreds = cal.number1_hundreds.maxIndex;
cal.number2.execute();
cal.number2.ones = cal.number2_ones.maxIndex;
cal.number2.execute();
cal.number2.tens = cal.number2_tens.maxIndex;
cal.number2.execute();
cal.number2.hundreds = cal.number2_hundreds.maxIndex;
cal.number1.execute();
cal.add.num1 = cal.number1.number;
cal.number2.execute();
cal.add.num2 = cal.number2.number;
cal.add.execute();
res = cal.add.sum;
}

};
#endif
