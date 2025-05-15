/* (c) https://github.com/MontiCore/monticore */
#ifndef TORCS_AGENT_NETWORK_REWARD
#define TORCS_AGENT_NETWORK_REWARD
#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif
#include "armadillo"
using namespace arma;
class torcs_agent_network_reward{
public:
colvec state;
bool isTerminal;
double reward;
void init()
{
state=colvec(29);
}
void execute()
{
double speedX = state(22-1)*300;
double angle = state(1-1)*3.1416;
double trackPos = state(21-1);
if((speedX < 0)){
speedX = 0;
}
reward = (speedX*(cos(angle)))-(speedX*(sin(angle)))-(speedX*(std::abs(trackPos)));
if(((std::abs(trackPos)) > 1)){
reward = -200;
}
for( auto i=2;i<=20;++i){
if(((std::abs(state(i-1))) > 1)){
reward = -200;
}
}
}

};
#endif
