/* (c) https://github.com/MontiCore/monticore */
#include "torcs_agent_network_reward_executor.h"

void torcs_agent_network_reward_executor::init() {
    instance.init();
}

torcs_agent_network_reward_output torcs_agent_network_reward_executor::execute(torcs_agent_network_reward_input input) {
    torcs_agent_network_reward_output output;

    instance.state = input.state;
    instance.isTerminal = input.isTerminal;

    instance.execute();

    output.reward = instance.reward;
    return output;
}
