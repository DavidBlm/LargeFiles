/* (c) https://github.com/MontiCore/monticore */
#ifndef CARTPOLE_MASTER
#define CARTPOLE_MASTER
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
#include "cartpole_master_dqn.h"
#include "cartpole_master_policy.h"
using namespace arma;
class cartpole_master{
public:
colvec state;
int action;
cartpole_master_dqn dqn;
cartpole_master_policy policy;
void init()
{
state=colvec(4);
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
