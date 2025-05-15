/* (c) https://github.com/MontiCore/monticore */
#ifndef TORCS_AGENT_TORCSAGENT_ACTOR
#define TORCS_AGENT_TORCSAGENT_ACTOR
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "CNNPredictor_torcs_agent_torcsAgent_actor.h"
#include "CNNTranslator.h"
using namespace arma;
class torcs_agent_torcsAgent_actor{
public:
CNNPredictor_torcs_agent_torcsAgent_actor_0 _predictor_0_;
colvec state;
colvec action;
void init()
{
state=colvec(29);
action=colvec(3);
}
void execute(){
    vector<float> state_ = CNNTranslator::translate(state);

    vector<float> action_(3);


    _predictor_0_.predict(state_, action_);

    action = CNNTranslator::translateToCol(action_, std::vector<size_t> {3});

}

};
#endif