/* (c) https://github.com/MontiCore/monticore */
#ifndef CARTPOLE_MASTER_DQN
#define CARTPOLE_MASTER_DQN
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "CNNPredictor_cartpole_master_dqn.h"
#include "CNNTranslator.h"
using namespace arma;
class cartpole_master_dqn{
public:
CNNPredictor_cartpole_master_dqn_0 _predictor_0_;
colvec state;
colvec qvalues;
void init()
{
state=colvec(4);
qvalues=colvec(2);
}
void execute(){
    vector<float> state_ = CNNTranslator::translate(state);

    vector<float> qvalues_(2);


    _predictor_0_.predict(state_, qvalues_);

    qvalues = CNNTranslator::translateToCol(qvalues_, std::vector<size_t> {2});

}

};
#endif
