# (c) https://github.com/MontiCore/monticore
import abc
import logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

class Environment:
    __metaclass__ = abc.ABCMeta

    def __init__(self):
        pass

    @abc.abstractmethod
    def reset(self):
        pass

    @abc.abstractmethod
    def step(self, action):
        pass

    @abc.abstractmethod
    def close(self):
        pass

import gym
class GymEnvironment(Environment):
    def __init__(self, env_name, **kwargs):
        super(GymEnvironment, self).__init__(**kwargs)
        self.__seed = 42
        self.__env = gym.make(env_name)
        self.__env.seed(self.__seed)

    @property
    def state_dim(self):
        return self.__env.observation_space.shape


    @property
    def number_of_actions(self):
        return self.__env.action_space.n

    @property
    def rewards_dtype(self):
        return 'float32'

    def reset(self):
        return self.__env.reset()

    def step(self, action):
        return self.__env.step(action)

    def close(self):
        self.__env.close()

    def action_space(self):
        self.__env.action_space

    def is_in_action_space(self, action):
        return self.__env.action_space.contains(action)

    def sample_action(self):
        return self.__env.action_space.sample()

    def render(self):
        self.__env.render()
