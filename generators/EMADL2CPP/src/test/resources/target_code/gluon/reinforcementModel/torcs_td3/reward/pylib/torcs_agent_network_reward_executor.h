/* (c) https://github.com/MontiCore/monticore */
#ifndef _TORCS_AGENT_NETWORK_REWARD_EXECUTOR_H_
#define _TORCS_AGENT_NETWORK_REWARD_EXECUTOR_H_
#include "armadillo"
#include "../torcs_agent_network_reward.h"

struct torcs_agent_network_reward_input {
    arma::colvec state;
    bool isTerminal;
};

struct torcs_agent_network_reward_output {
    double reward;
};

class torcs_agent_network_reward_executor {
private:
    torcs_agent_network_reward instance;
public:
    void init();
    torcs_agent_network_reward_output execute(torcs_agent_network_reward_input input);
};
#endif
