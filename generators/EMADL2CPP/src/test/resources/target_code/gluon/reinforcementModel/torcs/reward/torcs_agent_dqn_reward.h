/* (c) https://github.com/MontiCore/monticore */
#ifndef TORCS_AGENT_DQN_REWARD
#define TORCS_AGENT_DQN_REWARD
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
using namespace arma;
class torcs_agent_dqn_reward{
public:
colvec state;
bool isTerminal;
double reward;
void init()
{
state=colvec(5);
}
void execute()
{
double angle = state(1-1);
double speed = state(2-1);
reward = speed*(cos(angle));
}

};
#endif
