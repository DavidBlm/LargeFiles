/* (c) https://github.com/MontiCore/monticore */
#ifndef TORCS_AGENT_TORCSAGENT
#define TORCS_AGENT_TORCSAGENT
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "torcs_agent_torcsAgent_actor.h"
using namespace arma;
class torcs_agent_torcsAgent{
public:
colvec state;
colvec action;
torcs_agent_torcsAgent_actor actor;
void init()
{
state=colvec(29);
action=colvec(3);
actor.init();
}
void execute()
{
actor.state = state;
actor.execute();
action = actor.action;
}

};
#endif