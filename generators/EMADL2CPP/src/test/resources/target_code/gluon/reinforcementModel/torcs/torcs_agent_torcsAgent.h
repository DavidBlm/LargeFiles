/* (c) https://github.com/MontiCore/monticore */
#ifndef TORCS_AGENT_TORCSAGENT
#define TORCS_AGENT_TORCSAGENT
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "torcs_agent_torcsAgent_dqn.h"
#include "torcs_agent_torcsAgent_policy.h"
using namespace arma;
class torcs_agent_torcsAgent{
public:
colvec state;
int action;
torcs_agent_torcsAgent_dqn dqn;
torcs_agent_torcsAgent_policy policy;
void init()
{
state=colvec(5);
dqn.init();
policy.init();
}
void execute()
{
dqn.state = state;
dqn.execute();
policy.values = dqn.qvalues;
policy.execute();
action = policy.action;
}

};
#endif
