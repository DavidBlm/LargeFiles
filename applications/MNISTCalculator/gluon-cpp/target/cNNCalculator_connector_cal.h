#ifndef CNNCALCULATOR_CONNECTOR_CAL
#define CNNCALCULATOR_CONNECTOR_CAL
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "cNNCalculator_connector_cal_number1_ones.h"
#include "cNNCalculator_connector_cal_number1_tens.h"
#include "cNNCalculator_connector_cal_number1_hundreds.h"
#include "cNNCalculator_connector_cal_number2_ones.h"
#include "cNNCalculator_connector_cal_number2_tens.h"
#include "cNNCalculator_connector_cal_number2_hundreds.h"
#include "cNNCalculator_connector_cal_number1.h"
#include "cNNCalculator_connector_cal_number2.h"
#include "cNNCalculator_connector_cal_add.h"
using namespace arma;
class cNNCalculator_connector_cal{
public:
colvec in1_1;
colvec in1_2;
colvec in1_3;
colvec in2_1;
colvec in2_2;
colvec in2_3;
int out1;
cNNCalculator_connector_cal_number1_ones number1_ones;
cNNCalculator_connector_cal_number1_tens number1_tens;
cNNCalculator_connector_cal_number1_hundreds number1_hundreds;
cNNCalculator_connector_cal_number2_ones number2_ones;
cNNCalculator_connector_cal_number2_tens number2_tens;
cNNCalculator_connector_cal_number2_hundreds number2_hundreds;
cNNCalculator_connector_cal_number1 number1;
cNNCalculator_connector_cal_number2 number2;
cNNCalculator_connector_cal_add add;
void init()
{
in1_1=colvec(10);
in1_2=colvec(10);
in1_3=colvec(10);
in2_1=colvec(10);
in2_2=colvec(10);
in2_3=colvec(10);
number1_ones.init();
number1_tens.init();
number1_hundreds.init();
number2_ones.init();
number2_tens.init();
number2_hundreds.init();
number1.init();
number2.init();
add.init();
}
void execute()
{
}

};
#endif
