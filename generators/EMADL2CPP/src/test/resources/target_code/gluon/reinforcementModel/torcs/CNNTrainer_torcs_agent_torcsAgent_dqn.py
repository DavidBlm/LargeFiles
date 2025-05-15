# (c) https://github.com/MontiCore/monticore
from reinforcement_learning.agent import DqnAgent
from reinforcement_learning.util import AgentSignalHandler
from reinforcement_learning.cnnarch_logger import ArchLogger
import reinforcement_learning.environment
import CNNCreator_torcs_agent_torcsAgent_dqn

import os
import sys
import re
import time
import numpy as np
import mxnet as mx


def resume_session(sessions_dir):
    resume_session = False
    resume_directory = None
    if os.path.isdir(sessions_dir):
        regex = re.compile(r'\d\d\d\d-\d\d-\d\d-\d\d-\d\d')
        dir_content = os.listdir(sessions_dir)
        session_files = list(filter(regex.search, dir_content))
        session_files.sort(reverse=True)
        for d in session_files:
            interrupted_session_dir = os.path.join(sessions_dir, d, '.interrupted_session')
            if os.path.isdir(interrupted_session_dir):
                if sys.version.strip().split(".")[0] > '2':
                    resume = input('Interrupted session from {} found. Do you want to resume? (y/n) '.format(d))
                else:
                    resume = raw_input('Interrupted session from {} found. Do you want to resume? (y/n) '.format(d))
                if resume == 'y':
                    resume_session = True
                    resume_directory = interrupted_session_dir
                break
    return resume_session, resume_directory


if __name__ == "__main__":
    agent_name = 'torcs_agent_torcsAgent_dqn'
    # Prepare output directory and logger
    all_output_dir = os.path.join('model', agent_name)
    output_directory = os.path.join(
        all_output_dir,
        time.strftime('%Y-%m-%d-%H-%M-%S',
                      time.localtime(time.time())))
    ArchLogger.set_output_directory(output_directory)
    ArchLogger.set_logger_name(agent_name)
    ArchLogger.set_output_level(ArchLogger.INFO)

    env_params = {
        'ros_node_name': 'torcs_agent_torcsAgent_dqnTrainerNode',
        'state_topic': 'preprocessor_state',
        'action_topic': 'postprocessor_action',
        'reset_topic': 'torcs_reset',
        'terminal_state_topic': 'prepocessor_is_terminal',
    }
    env = reinforcement_learning.environment.RosEnvironment(**env_params)

    context = mx.cpu()
    initializer = mx.init.Normal()
    qnet_creator = CNNCreator_torcs_agent_torcsAgent_dqn.CNNCreator_torcs_agent_torcsAgent_dqn()
    qnet_creator.setWeightInitializer(initializer)
    qnet_creator.construct([context])

    agent_params = {
        'environment': env,
        'replay_memory_params': {
            'method': 'buffer',
            'memory_size': 1000000,
            'sample_size': 32,
            'state_dtype': 'float32',
            'action_dtype': 'uint8',
            'rewards_dtype': 'float32'

        },
        'strategy_params': {
            'method':'epsgreedy',
            'epsilon': 1.0,
            'min_epsilon': 0.01,
            'epsilon_decay_method': 'linear',
            'epsilon_decay': 1.0E-4,
        },
        'agent_name': agent_name,
        'verbose': True,
        'output_directory': output_directory,
        'state_dim': (5,),
        'action_dim': (30,),
        'ctx': 'cpu',
        'discount_factor': 0.999,
        'training_episodes': 20000,
        'train_interval': 1,
        'snapshot_interval': 1000,
        'max_episode_step': 999999999,
        'qnet':qnet_creator.networks[0],
        'use_fix_target': True,
        'target_update_interval': 500,
        'loss_function': 'huber',
        'optimizer': 'rmsprop',
        'optimizer_params': {
                'learning_rate': 0.001        },
        'double_dqn': True,
    }

    resume, resume_directory = resume_session(all_output_dir)

    if resume:
        output_directory, _ = os.path.split(resume_directory)
        ArchLogger.set_output_directory(output_directory)
        resume_agent_params = {
            'session_dir': resume_directory,
            'environment': env,
            'net': qnet_creator.networks[0],
        }
        agent = DqnAgent.resume_from_session(**resume_agent_params)
    else:
        agent = DqnAgent(**agent_params)

    signal_handler = AgentSignalHandler()
    signal_handler.register_agent(agent)

    train_successful = agent.train()

    if train_successful:
        agent.export_best_network(path=str(qnet_creator.get_model_dir(epoch=0) / 'model_0_newest'), epoch=0)
