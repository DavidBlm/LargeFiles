/* (c) https://github.com/MontiCore/monticore */
#ifndef TORCS_AGENT_TORCSAGENT_DQN
#define TORCS_AGENT_TORCSAGENT_DQN
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "CNNPredictor_torcs_agent_torcsAgent_dqn.h"
#include "CNNTranslator.h"
using namespace arma;
class torcs_agent_torcsAgent_dqn{
const int discrete_actions = 30;
public:
CNNPredictor_torcs_agent_torcsAgent_dqn_0 _predictor_0_;
colvec state;
colvec qvalues;
void init()
{
state=colvec(5);
qvalues=colvec(discrete_actions);
}
void execute(){
    vector<float> state_ = CNNTranslator::translate(state);

    vector<float> qvalues_(30);


    _predictor_0_.predict(state_, qvalues_);

    qvalues = CNNTranslator::translateToCol(qvalues_, std::vector<size_t> {30});

}

};
#endif
