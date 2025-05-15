/* (c) https://github.com/MontiCore/monticore */
#ifndef TORCS_AGENT_TORCSAGENT_POLICY
#define TORCS_AGENT_TORCSAGENT_POLICY
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
using namespace arma;
class torcs_agent_torcsAgent_policy{
const int n = 30;
public:
colvec values;
int action;
void init()
{
values=colvec(n);
}
void execute()
{
int best_action = 0;
double value_of_best_action = values(1-1);
for( auto i=2;i<=n;++i){
if((values(i-1) > value_of_best_action)){
best_action = i-1;
value_of_best_action = values(i-1);
}
}
action = best_action;
}

};
#endif
