# (c) https://github.com/MontiCore/monticore
import signal
import sys
import numpy as np
import matplotlib.pyplot as plt
from matplotlib import style
import time
import os
import mxnet as mx
from mxnet import gluon, nd
import reinforcement_learning.cnnarch_logger as cnnarch_logger

LOSS_FUNCTIONS = {
        'l1': gluon.loss.L1Loss(),
        'l2': gluon.loss.L2Loss(),
        'huber': gluon.loss.HuberLoss(),
        'softmax_cross_entropy': gluon.loss.SoftmaxCrossEntropyLoss(),
        'sigmoid_cross_entropy': gluon.loss.SigmoidBinaryCrossEntropyLoss()}


def make_directory_if_not_exist(directory):
    if not os.path.exists(directory):
        os.makedirs(directory)


def copy_net(net, input_state_dim, ctx):
    assert isinstance(net, gluon.HybridBlock)
    assert type(net.__class__) is type

    net2 = net.__class__()
    net2.collect_params().initialize(mx.init.Zero(), ctx=ctx)
    net2.hybridize()
    net2(mx.nd.ones((1,) + input_state_dim, ctx=ctx))

    params_of_net = [p.data() for _, p in net.collect_params().items()]
    for i, (_, p) in enumerate(net2.collect_params().items()):
        p.set_data(params_of_net[i])

    return net2


def copy_net_with_two_inputs(net, input_state_dim1, input_state_dim2, ctx):
    assert isinstance(net, gluon.HybridBlock)
    assert type(net.__class__) is type

    net2 = net.__class__()
    net2.collect_params().initialize(mx.init.Zero(), ctx=ctx)
    net2.hybridize()
    net2(
        nd.ones((1,) + input_state_dim1, ctx=ctx),
        nd.ones((1,) + input_state_dim2, ctx=ctx))

    params_of_net = [p.data() for _, p in net.collect_params().items()]
    for i, (_, p) in enumerate(net2.collect_params().items()):
        p.set_data(params_of_net[i])

    return net2


def get_loss_function(loss_function_name):
    if loss_function_name not in LOSS_FUNCTIONS:
        raise ValueError('Loss function does not exist')
    return LOSS_FUNCTIONS[loss_function_name]


class AgentSignalHandler(object):
    def __init__(self):
        signal.signal(signal.SIGINT, self.interrupt_training)
        self.__agent = None
        self.__times_interrupted = 0

    def register_agent(self, agent):
        self.__agent = agent

    def interrupt_training(self, sig, frame):
        self.__times_interrupted = self.__times_interrupted + 1
        if self.__times_interrupted <= 3:
            if self.__agent:
                self.__agent.set_interrupt_flag(True)
        else:
            print('Interrupt called three times: Force quit')
            sys.exit(1)

style.use('fivethirtyeight')


class TrainingStats(object):
    def __init__(self, max_episodes):
        self._logger = cnnarch_logger.ArchLogger.get_logger()
        self._max_episodes = max_episodes
        self._all_total_rewards = np.zeros((max_episodes,))
        self._all_eps = np.zeros((max_episodes,))
        self._all_time = np.zeros((max_episodes,))
        self._all_mean_reward_last_100_episodes = np.zeros((max_episodes,))

    @property
    def logger(self):
        return self._logger

    @logger.setter
    def logger(self, logger):
        self._logger = logger

    @logger.deleter
    def logger(self):
        self._logger = None

    def add_total_reward(self, episode, total_reward):
        self._all_total_rewards[episode] = total_reward

    def add_eps(self, episode, eps):
        self._all_eps[episode] = eps

    def add_time(self, episode, time):
        self._all_time[episode] = time

    def add_mean_reward_last_100(self, episode, mean_reward):
        self._all_mean_reward_last_100_episodes[episode] = mean_reward

    def log_episode(self, *args):
        raise NotImplementedError

    def mean_of_reward(self, cur_episode, last=100):
        if cur_episode > 0:
            reward_last_100 =\
                self._all_total_rewards[max(0, cur_episode-last):cur_episode]
            return np.mean(reward_last_100)
        else:
            return self._all_total_rewards[0]

    def save(self, path, episode=None):
        if episode is None:
            episode = self._max_episodes
        np.save(os.path.join(path, 'total_rewards'), self._all_total_rewards[:episode])
        np.save(os.path.join(path, 'eps'), self._all_eps[:episode])
        np.save(os.path.join(path, 'time'), self._all_time[:episode])
        np.save(
            os.path.join(path, 'mean_reward'),
            self._all_mean_reward_last_100_episodes[:episode])

    def _log_episode(self, episode, start_time, training_steps, eps, reward):
        self.add_eps(episode, eps)
        self.add_total_reward(episode, reward)
        end = time.time()
        mean_reward_last_100 = self.mean_of_reward(episode, last=100)
        time_elapsed = end - start_time
        self.add_time(episode, time_elapsed)
        self.add_mean_reward_last_100(episode, mean_reward_last_100)
        return ('Episode: %d, Total Reward: %.3f, '
                'Avg. Reward Last 100 Episodes: %.3f, {}, '
                'Time: %.3f, Training Steps: %d, Eps: %.3f') % (
                    episode, reward, mean_reward_last_100, time_elapsed,
                    training_steps, eps), mean_reward_last_100


class DqnTrainingStats(TrainingStats):
    def __init__(self, max_episodes):
        super(DqnTrainingStats, self).__init__(max_episodes)
        self._all_avg_loss = np.zeros((max_episodes,))

    def add_avg_loss(self, episode, avg_loss):
        self._all_avg_loss[episode] = avg_loss

    def log_episode(
        self, episode, start_time, training_steps, avg_loss, eps, reward
    ):
        self.add_avg_loss(episode, avg_loss)

        info, avg_reward = self._log_episode(
            episode, start_time, training_steps, eps, reward)
        info = info.format(('Avg. Loss: %.3f') % (avg_loss))

        self._logger.info(info)
        return avg_reward

    def save_stats(self, path, episode=None):
        if episode is None:
            episode = self._max_episodes

        all_total_rewards = self._all_total_rewards[:episode]
        all_avg_loss = self._all_avg_loss[:episode]
        all_eps = self._all_eps[:episode]
        all_mean_reward_last_100_episodes = self._all_mean_reward_last_100_episodes[:episode]

        fig = plt.figure(figsize=(20, 20))

        sub_rewards = fig.add_subplot(221)
        sub_rewards.set_title('Total Rewards per episode')
        sub_rewards.plot(
            np.arange(episode), all_total_rewards)

        sub_loss = fig.add_subplot(222)
        sub_loss.set_title('Avg. Loss per episode')
        sub_loss.plot(np.arange(episode), all_avg_loss)

        sub_eps = fig.add_subplot(223)
        sub_eps.set_title('Epsilon per episode')
        sub_eps.plot(np.arange(episode), all_eps)

        sub_rewards = fig.add_subplot(224)
        sub_rewards.set_title('Avg. mean reward of last 100 episodes')
        sub_rewards.plot(np.arange(episode),
                         all_mean_reward_last_100_episodes)

        self.save(path, episode=episode)
        plt.savefig(os.path.join(path, 'stats.pdf'))

    def save(self, path, episode=None):
        if episode is None:
            episode = self._max_episodes
        super(DqnTrainingStats, self).save(path, episode=episode)
        np.save(os.path.join(path, 'avg_loss'), self._all_avg_loss[:episode])


class DdpgTrainingStats(TrainingStats):
    def __init__(self, max_episodes):
        super(DdpgTrainingStats, self).__init__(max_episodes)
        self._all_avg_critic_loss = np.zeros((max_episodes,))
        self._all_avg_actor_loss = np.zeros((max_episodes,))
        self._all_avg_qvalues = np.zeros((max_episodes,))

    def add_avg_critic_loss(self, episode, avg_critic_loss):
        self._all_avg_critic_loss[episode] = avg_critic_loss

    def add_avg_actor_loss(self, episode, avg_actor_loss):
        self._all_avg_actor_loss[episode] = avg_actor_loss

    def add_avg_qvalues(self, episode, avg_qvalues):
        self._all_avg_qvalues[episode] = avg_qvalues

    def log_episode(
        self, episode, start_time, training_steps, actor_loss,
        critic_loss, qvalues, eps, reward
    ):
        self.add_avg_actor_loss(episode, actor_loss)
        self.add_avg_critic_loss(episode, critic_loss)
        self.add_avg_qvalues(episode, qvalues)

        info, avg_reward = self._log_episode(
            episode, start_time, training_steps, eps, reward)
        info = info.format((
            'Avg. Actor Loss: %.3f '
            'Avg. Critic Loss: %.3f '
            'Avg. Q-Values: %.3f') % (actor_loss, critic_loss, qvalues))

        self.logger.info(info)
        return avg_reward

    def save(self, path, episode=None):
        if episode is None:
            episode = self._max_episodes
        super(DdpgTrainingStats, self).save(path, episode=episode)
        np.save(os.path.join(
            path, 'avg_critic_loss'), self._all_avg_critic_loss[:episode])
        np.save(os.path.join(path, 'avg_actor_loss'), self._all_avg_actor_loss[:episode])
        np.save(os.path.join(path, 'avg_qvalues'), self._all_avg_qvalues[:episode])

    def save_stats(self, path, episode=None):
        if episode is None:
            episode = self._max_episodes

        all_total_rewards = self._all_total_rewards[:episode]
        all_avg_actor_loss = self._all_avg_actor_loss[:episode]
        all_avg_critic_loss = self._all_avg_critic_loss[:episode]
        all_avg_qvalues = self._all_avg_qvalues[:episode]
        all_eps = self._all_eps[:episode]
        all_mean_reward_last_100_episodes = self._all_mean_reward_last_100_episodes[:episode]

        fig = plt.figure(figsize=(120, 120))

        sub_rewards = fig.add_subplot(321)
        sub_rewards.set_title('Total Rewards per episode')
        sub_rewards.plot(
            np.arange(episode), all_total_rewards)

        sub_actor_loss = fig.add_subplot(322)
        sub_actor_loss.set_title('Avg. Actor Loss per episode')
        sub_actor_loss.plot(
            np.arange(episode), all_avg_actor_loss)

        sub_critic_loss = fig.add_subplot(323)
        sub_critic_loss.set_title('Avg. Critic Loss per episode')
        sub_critic_loss.plot(
            np.arange(episode), all_avg_critic_loss)

        sub_qvalues = fig.add_subplot(324)
        sub_qvalues.set_title('Avg. QValues per episode')
        sub_qvalues.plot(
            np.arange(episode), all_avg_qvalues)

        sub_eps = fig.add_subplot(325)
        sub_eps.set_title('Epsilon per episode')
        sub_eps.plot(np.arange(episode), all_eps)

        sub_rewards = fig.add_subplot(326)
        sub_rewards.set_title('Avg. mean reward of last 100 episodes')
        sub_rewards.plot(np.arange(episode),
                         all_mean_reward_last_100_episodes)

        self.save(path, episode=episode)
        plt.savefig(os.path.join(path, 'stats.pdf'))
