# (c) https://github.com/MontiCore/monticore
import mxnet as mx
import numpy as np
import time
import os
import subprocess #library to call shell script
import shlex #library to pass variable to shell script
import sys
import reinforcement_learning.util as util
import matplotlib.pyplot as plt
import pyprind
from reinforcement_learning.cnnarch_logger import ArchLogger
from reinforcement_learning.replay_memory import ReplayMemoryBuilder
from reinforcement_learning.strategy import StrategyBuilder
from reinforcement_learning.util import copy_net, get_loss_function,\
    copy_net_with_two_inputs, DdpgTrainingStats, DqnTrainingStats,\
    make_directory_if_not_exist
from mxnet import nd, gluon, autograd


class Agent(object):
    def __init__(
        self,
        environment,
        replay_memory_params,
        strategy_params,
        state_dim,
        action_dim,
        ctx=None,
        discount_factor=.9,
        training_episodes=50,
        train_interval=1,
        start_training=0,
        snapshot_interval=200,
        agent_name='Agent',
        max_episode_step=99999,
        evaluation_samples=1000,
        self_play='no',
        output_directory='model_parameters',
        verbose=True,
        target_score=None
    ):
        assert 0 < discount_factor <= 1,\
            'Discount factor must be between 0 and 1'
        assert train_interval > 0, 'Train interval must be greater 0'
        assert snapshot_interval > 0, 'Snapshot interval must be greater 0'
        assert max_episode_step > 0,\
            'Maximal steps per episode must be greater 0'
        assert training_episodes > 0, 'Trainings episode must be greater 0'
        assert replay_memory_params is not None,\
            'Replay memory parameter not set'
        assert type(state_dim) is tuple, 'State dimension is not a tuple'
        assert type(action_dim) is tuple, 'Action dimension is not a tuple'

        self._logger = ArchLogger.get_logger()
        self._ctx = mx.gpu() if ctx == 'gpu' else mx.cpu()
        self._environment = environment
        self._discount_factor = discount_factor
        self._training_episodes = training_episodes
        self._train_interval = train_interval
        self._verbose = verbose
        self._state_dim = state_dim

        replay_memory_params['state_dim'] = state_dim
        replay_memory_params['action_dim'] = action_dim
        self._replay_memory_params = replay_memory_params
        rm_builder = ReplayMemoryBuilder()
        self._memory = rm_builder.build_by_params(**replay_memory_params)
        self._minibatch_size = self._memory.sample_size
        self._action_dim = action_dim

        strategy_params['action_dim'] = self._action_dim
        self._strategy_params = strategy_params
        strategy_builder = StrategyBuilder()
        self._strategy = strategy_builder.build_by_params(**strategy_params)
        self._agent_name = agent_name
        self._snapshot_interval = snapshot_interval
        self._creation_time = time.time()
        self._max_episode_step = max_episode_step
        self._start_training = start_training
        self._output_directory = output_directory
        self._target_score = target_score

        self._evaluation_samples = evaluation_samples
        self._self_play = self_play
        self._best_avg_score = -np.infty
        self._best_net = None

        self._interrupt_flag = False
        self._training_stats = None

        # Training Context
        self._current_episode = 0
        self._total_steps = 0

    @property
    def current_episode(self):
        return self._current_episode

    @property
    def environment(self):
        return self._environment

    def save_config_file(self):
        import json
        make_directory_if_not_exist(self._output_directory)
        filename = os.path.join(self._output_directory, 'config.json')
        config = self._make_config_dict()
        with open(filename, mode='w') as fp:
            json.dump(config, fp, indent=4)

    def set_interrupt_flag(self, interrupt):
        self._interrupt_flag = interrupt

    def _interrupt_training(self):
        import pickle
        self._logger.info('Training interrupted; Store state for resuming')
        session_dir = self._get_session_dir()
        agent_session_file = os.path.join(session_dir, 'agent.p')
        logger = self._logger

        self._training_stats.save_stats(self._output_directory, episode=self._current_episode)

        self._make_pickle_ready(session_dir)

        with open(agent_session_file, 'wb') as f:
            pickle.dump(self, f, protocol=2)
        logger.info('State successfully stored')

    def _make_pickle_ready(self, session_dir):
        del self._training_stats.logger
        self._environment.close()
        self._environment = None
        self._export_net(self._best_net, 'best_net', filedir=session_dir)
        self._logger = None
        self._best_net = None

    def _make_config_dict(self):
        config = dict()
        config['state_dim'] = self._state_dim
        config['action_dim'] = self._action_dim
        config['ctx'] = str(self._ctx)
        config['discount_factor'] = self._discount_factor
        config['strategy_params'] = self._strategy_params
        config['replay_memory_params'] = self._replay_memory_params
        config['training_episodes'] = self._training_episodes
        config['start_training'] = self._start_training
        config['evaluation_samples'] = self._evaluation_samples
        config['self_play'] = self._self_play 
        config['train_interval'] = self._train_interval
        config['snapshot_interval'] = self._snapshot_interval
        config['agent_name'] = self._agent_name
        config['max_episode_step'] = self._max_episode_step
        config['output_directory'] = self._output_directory
        config['verbose'] = self._verbose
        config['target_score'] = self._target_score
        return config

    def _adjust_optimizer_params(self, optimizer_params):
        if 'weight_decay' in optimizer_params:
            optimizer_params['wd'] = optimizer_params['weight_decay']
            del optimizer_params['weight_decay']
        if 'learning_rate_decay' in optimizer_params:
            min_learning_rate = 1e-8
            if 'learning_rate_minimum' in optimizer_params:
                min_learning_rate = optimizer_params['learning_rate_minimum']
                del optimizer_params['learning_rate_minimum']
            optimizer_params['lr_scheduler'] = mx.lr_scheduler.FactorScheduler(
                optimizer_params['step_size'],
                factor=optimizer_params['learning_rate_decay'],
                stop_factor_lr=min_learning_rate)
            del optimizer_params['step_size']
            del optimizer_params['learning_rate_decay']

        return optimizer_params

    def _sample_from_memory(self):
        states, actions, rewards, next_states, terminals\
            = self._memory.sample(batch_size=self._minibatch_size)
        states = nd.array(states, ctx=self._ctx)
        actions = nd.array(actions, ctx=self._ctx)
        rewards = nd.array(rewards, ctx=self._ctx)
        next_states = nd.array(next_states, ctx=self._ctx)
        terminals = nd.array(terminals, ctx=self._ctx)
        return states, actions, rewards, next_states, terminals

    def evaluate(self, target=None, sample_games=100, verbose=True):
        if sample_games <= 0:
            return 0

        target = self._target_score if target is None else target
        if target:
            target_achieved = 0
        total_reward = 0

        self._logger.info('Sampling from {} games...'.format(sample_games))
        for g in pyprind.prog_bar(range(sample_games)):
            state = self._environment.reset()
            step = 0
            game_reward = 0
            terminal = False
            while not terminal and (step < self._max_episode_step):
                action = self.get_next_action(state)
                state, reward, terminal, _ = self._environment.step(action)
                game_reward += reward
                step += 1

            if verbose:
                info = 'Game %d: Reward %f' % (g, game_reward)
                self._logger.debug(info)
            if target:
                if game_reward >= target:
                    target_achieved += 1
            total_reward += game_reward

        avg_reward = float(total_reward)/float(sample_games)
        info = 'Avg. Reward: %f' % avg_reward
        if target:
            target_achieved_ratio = int(
                (float(target_achieved)/float(sample_games))*100)
            info += '; Target Achieved in %d%% of games'\
                % (target_achieved_ratio)

        if verbose:
            self._logger.info(info)
        return avg_reward

    def _do_snapshot_if_in_interval(self, episode):
        do_snapshot =\
            (episode != 0 and (episode % self._snapshot_interval == 0))
        if do_snapshot:
            self.save_parameters(episode=episode)
            self._evaluate()
            if self._self_play == 'yes':
                print("Self Play activated")
                path = os.getcwd()
                file = os.path.join(os.getcwd(), os.listdir(os.getcwd())[0])
                home_path = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(file))))))
                os.chdir(home_path)
                snapshot_param = str(self._snapshot_interval)
                subprocess.check_call(['./update_agent.sh', snapshot_param])
                os.chdir(path)
            elif self._self_play == 'no':
                print("Self play deactivated")

    
    def _evaluate(self, verbose=True):
        avg_reward = self.evaluate(
            sample_games=self._evaluation_samples, verbose=False)
        info = 'Evaluation -> Average Reward in {} games: {}'.format(
            self._evaluation_samples, avg_reward)

        if self._best_avg_score is None or self._best_avg_score <= avg_reward:
            self._save_current_as_best_net()
            self._best_avg_score = avg_reward
        if verbose:
            self._logger.info(info)

    def _is_target_reached(self, avg_reward):
        return self._target_score is not None\
            and avg_reward > self._target_score

    def _do_training(self):
        return (self._total_steps % self._train_interval == 0) and\
            (self._memory.is_sample_possible(self._minibatch_size)) and\
            (self._current_episode >= self._start_training)

    def _check_interrupt_routine(self):
        if self._interrupt_flag:
            self._interrupt_flag = False
            self._interrupt_training()
            return True
        return False

    def _is_target_reached(self, avg_reward):
        return self._target_score is not None\
            and avg_reward > self._target_score

    def _export_net(self, net, filename, filedir=None, episode=None):
        assert self._output_directory
        assert isinstance(net, gluon.HybridBlock)
        make_directory_if_not_exist(self._output_directory)
        filedir = self._output_directory if filedir is None else filedir
        filename = os.path.join(filedir, filename)

        if episode is not None:
            filename = filename + '-ep{}'.format(episode)

        net.export(filename, epoch=0)
        net.save_parameters(filename + '.params')

    def export_best_network(self, path=None, epoch=0):
        path = os.path.join(self._output_directory, 'best_network')\
            if path is None else path
        self._logger.info(
            'Saving best network with average reward of {}'.format(
                self._best_avg_score))
        self._best_net.export(path, epoch=epoch)

    def _get_session_dir(self):
        session_dir = os.path.join(
            self._output_directory, '.interrupted_session')
        make_directory_if_not_exist(session_dir)
        return session_dir

    def _save_current_as_best_net(self):
        raise NotImplementedError

    def get_next_action(self, state):
        raise NotImplementedError

    def save_parameters(self, episode):
        raise NotImplementedError

    def train(self, episodes=None):
        raise NotImplementedError


class DdpgAgent(Agent):
    def __init__(
        self,
        actor,
        critic,
        environment,
        replay_memory_params,
        strategy_params,
        state_dim,
        action_dim,
        soft_target_update_rate=.001,
        actor_optimizer='adam',
        actor_optimizer_params={'learning_rate': 0.0001},
        critic_optimizer='adam',
        critic_optimizer_params={'learning_rate': 0.001},
        ctx=None,
        discount_factor=.9,
        training_episodes=50,
        start_training=20,
        train_interval=1,
        snapshot_interval=200,
        agent_name='DdpgAgent',
        max_episode_step=9999,
        evaluation_samples=100,
        self_play='no',
        output_directory='model_parameters',
        verbose=True,
        target_score=None
    ):
        super(DdpgAgent, self).__init__(
            environment=environment, replay_memory_params=replay_memory_params,
            strategy_params=strategy_params, state_dim=state_dim,
            action_dim=action_dim, ctx=ctx, discount_factor=discount_factor,
            training_episodes=training_episodes, start_training=start_training,
            train_interval=train_interval,
            snapshot_interval=snapshot_interval, agent_name=agent_name,
            max_episode_step=max_episode_step,
            output_directory=output_directory, verbose=verbose,
            target_score=target_score, evaluation_samples=evaluation_samples, self_play=self_play)
        assert critic is not None, 'Critic not set'
        assert actor is not None, 'Actor is not set'
        assert soft_target_update_rate > 0,\
            'Target update must be greater zero'
        assert actor_optimizer is not None, 'No actor optimizer set'
        assert critic_optimizer is not None, 'No critic optimizer set'

        self._actor = actor
        self._critic = critic

        self._actor_target = self._copy_actor()
        self._critic_target = self._copy_critic()

        self._actor_optimizer = actor_optimizer
        self._actor_optimizer_params = self._adjust_optimizer_params(
            actor_optimizer_params)

        self._critic_optimizer = critic_optimizer
        self._critic_optimizer_params = self._adjust_optimizer_params(
            critic_optimizer_params)

        self._soft_target_update_rate = soft_target_update_rate

        self._logger.info(
            'Agent created with following parameters: {}'.format(
                self._make_config_dict()))

        self._best_net = self._copy_actor()

        self._training_stats = DdpgTrainingStats(self._training_episodes)

    def _make_pickle_ready(self, session_dir):
        super(DdpgAgent, self)._make_pickle_ready(session_dir)
        self._export_net(self._actor, 'current_actor')

        self._export_net(self._actor, 'actor', filedir=session_dir)
        self._actor = None
        self._export_net(self._critic, 'critic', filedir=session_dir)
        self._critic = None
        self._export_net(
            self._actor_target, 'actor_target', filedir=session_dir)
        self._actor_target = None
        self._export_net(
            self._critic_target, 'critic_target', filedir=session_dir)
        self._critic_target = None

    @classmethod
    def resume_from_session(cls, session_dir, actor, critic, environment):
        import pickle
        if not os.path.exists(session_dir):
            raise ValueError('Session directory does not exist')

        files = dict()
        files['agent'] = os.path.join(session_dir, 'agent.p')
        files['best_net_params'] = os.path.join(session_dir, 'best_net.params')
        files['actor_net_params'] = os.path.join(session_dir, 'actor.params')
        files['actor_target_net_params'] = os.path.join(
            session_dir, 'actor_target.params')
        files['critic_net_params'] = os.path.join(session_dir, 'critic.params')
        files['critic_target_net_params'] = os.path.join(
            session_dir, 'critic_target.params')

        for file in files.values():
            if not os.path.exists(file):
                raise ValueError(
                    'Session directory is not complete: {} is missing'
                    .format(file))

        with open(files['agent'], 'rb') as f:
            agent = pickle.load(f)

        agent._environment = environment

        agent._actor = actor
        agent._actor.load_parameters(files['actor_net_params'], agent._ctx)
        agent._actor.hybridize()
        agent._actor(nd.random_normal(
            shape=((1,) + agent._state_dim), ctx=agent._ctx))

        agent._best_net = copy_net(agent._actor, agent._state_dim, agent._ctx)
        agent._best_net.load_parameters(files['best_net_params'], agent._ctx)

        agent._actor_target = copy_net(
            agent._actor, agent._state_dim, agent._ctx)
        agent._actor_target.load_parameters(files['actor_target_net_params'])

        agent._critic = critic
        agent._critic.load_parameters(files['critic_net_params'], agent._ctx)
        agent._critic.hybridize()
        agent._critic(
            nd.random_normal(shape=((1,) + agent._state_dim), ctx=agent._ctx),
            nd.random_normal(shape=((1,) + agent._action_dim), ctx=agent._ctx))

        agent._critic_target = copy_net_with_two_inputs(
            agent._critic, agent._state_dim, agent._action_dim, agent._ctx)
        agent._critic_target.load_parameters(files['critic_target_net_params'])

        agent._logger = ArchLogger.get_logger()
        agent._training_stats.logger = ArchLogger.get_logger()
        agent._logger.info('Agent was retrieved; Training can be continued')

        return agent

    def _save_current_as_best_net(self):
        self._best_net = self._copy_actor()

    def get_next_action(self, state):
        action = self._actor(nd.array([state], ctx=self._ctx))
        return action[0][0][0].asnumpy()

    def save_parameters(self, episode):
        self._export_net(
            self._actor, self._agent_name + '_actor', episode=episode)

    def train(self, episodes=None):
        self.save_config_file()
        self._logger.info("--- Start DDPG training ---")
        episodes = \
            episodes if episodes is not None else self._training_episodes

        resume = (self._current_episode > 0)
        if resume:
            self._logger.info("Training session resumed")
            self._logger.info(
                "Starting from episode {}".format(self._current_episode))
        else:
            self._training_stats = DdpgTrainingStats(episodes)

            # Initialize target Q' and mu'
            self._actor_target = self._copy_actor()
            self._critic_target = self._copy_critic()

        # Initialize l2 loss for critic network
        l2_loss = gluon.loss.L2Loss()

        # Initialize critic and actor trainer
        trainer_actor = gluon.Trainer(
            self._actor.collect_params(), self._actor_optimizer,
            self._actor_optimizer_params)
        trainer_critic = gluon.Trainer(
            self._critic.collect_params(), self._critic_optimizer,
            self._critic_optimizer_params)

        # For episode=1..n
        while self._current_episode < episodes:
            # Check interrupt flag
            if self._check_interrupt_routine():
                return False

            # Initialize new episode
            step = 0
            episode_reward = 0
            start = time.time()
            episode_critic_loss = 0
            episode_actor_loss = 0
            episode_avg_q_value = 0
            training_steps = 0

            # Get initialial observation state s
            state = self._environment.reset()

            # For step=1..T
            while step < self._max_episode_step:
                # Select an action a = mu(s) + N(step) according to current
                #  actor and exploration noise N according to strategy
                action = self._strategy.select_action(
                    self.get_next_action(state))
                self._strategy.decay(self._current_episode)

                # Execute action a and observe reward r and next state ns
                next_state, reward, terminal, _ = \
                    self._environment.step(action)

                self._logger.debug(
                    'Applied action {} with reward {}'.format(action, reward))

                # Store transition (s,a,r,ns) in replay buffer
                self._memory.append(
                    state, action, reward, next_state, terminal)

                if self._do_training():
                    # Sample random minibatch of b transitions
                    # (s_i, a_i, r_i, s_(i+1)) from replay buffer
                    states, actions, rewards, next_states, terminals =\
                         self._sample_from_memory()

                    actor_target_actions = self._actor_target(next_states)
                    critic_target_qvalues = self._critic_target(
                        next_states, actor_target_actions[0][0])

                    rewards = rewards.reshape(self._minibatch_size, 1)
                    terminals = terminals.reshape(self._minibatch_size, 1)

                    # y = r_i + discount * Q'(s_(i+1), mu'(s_(i+1)))
                    y = rewards + (1.0 - terminals) * self._discount_factor\
                        * critic_target_qvalues[0][0]

                    # Train the critic network
                    with autograd.record():
                        qvalues = self._critic(states, actions)
                        critic_loss = l2_loss(qvalues[0][0], y)
                    critic_loss.backward()
                    trainer_critic.step(self._minibatch_size)

                    # Train the actor network
                    # Temporary critic so that gluon trainer does not mess
                    # with critic parameters
                    tmp_critic = self._copy_critic()
                    with autograd.record():
                        # For maximizing qvalues we have to multiply with -1
                        # as we use a minimizer
                        actor_loss = -tmp_critic(
                            states, self._actor(states)[0][0])[0][0].mean()
                    actor_loss.backward()
                    trainer_actor.step(self._minibatch_size)

                    # Update target networks:
                    self._actor_target = self._soft_update(
                        self._actor, self._actor_target,
                        self._soft_target_update_rate)
                    self._critic_target = self._soft_update(
                        self._critic, self._critic_target,
                        self._soft_target_update_rate)

                    # Update statistics
                    episode_critic_loss +=\
                        np.sum(critic_loss.asnumpy()) / self._minibatch_size
                    episode_actor_loss +=\
                        np.sum(actor_loss.asnumpy()) / self._minibatch_size
                    episode_avg_q_value +=\
                         np.sum(qvalues[0][0].asnumpy()) / self._minibatch_size

                    training_steps += 1

                episode_reward += reward
                step += 1
                self._total_steps += 1
                state = next_state

                if terminal:
                    # Reset the strategy
                    self._strategy.reset()
                    break

            # Log the episode results
            episode_actor_loss = 0 if training_steps == 0\
                else (episode_actor_loss / training_steps)
            episode_critic_loss = 0 if training_steps == 0\
                else (episode_critic_loss / training_steps)
            episode_avg_q_value = 0 if training_steps == 0\
                else (episode_avg_q_value / training_steps)

            avg_reward = self._training_stats.log_episode(
                self._current_episode, start, training_steps,
                episode_actor_loss, episode_critic_loss, episode_avg_q_value,
                self._strategy.cur_eps, episode_reward)

            self._do_snapshot_if_in_interval(self._current_episode)

            if self._is_target_reached(avg_reward):
                self._logger.info(
                    'Target score is reached in average; Training is stopped')
                break

            self._current_episode += 1

        self._evaluate()
        self.save_parameters(episode=self._current_episode)
        self.export_best_network()
        self._training_stats.save_stats(self._output_directory)
        self._logger.info('--------- Training finished ---------')
        return True

    def _make_config_dict(self):
        config = super(DdpgAgent, self)._make_config_dict()
        config['soft_target_update_rate'] = self._soft_target_update_rate
        config['actor_optimizer'] = self._actor_optimizer
        config['actor_optimizer_params'] = self._actor_optimizer_params
        config['critic_optimizer'] = self._critic_optimizer
        config['critic_optimizer_params'] = self._critic_optimizer_params
        return config

    def _soft_update(self, net, target, tau):
        net_params = [p.data() for _, p in net.collect_params().items()]
        for i, (_, p) in enumerate(target.collect_params().items()):
            target_params = p.data()
            p.set_data((1.0 - tau) * target_params + tau * net_params[i])
        return target

    def _copy_actor(self):
        assert self._actor is not None
        assert self._ctx is not None
        assert type(self._state_dim) is tuple
        return copy_net(self._actor, self._state_dim, ctx=self._ctx)

    def _copy_critic(self):
        assert self._critic is not None
        assert self._ctx is not None
        assert type(self._state_dim) is tuple
        assert type(self._action_dim) is tuple
        return copy_net_with_two_inputs(
            self._critic, self._state_dim, self._action_dim, ctx=self._ctx)


class TwinDelayedDdpgAgent(DdpgAgent):
    def __init__(
        self,
        actor,
        critic,
        environment,
        replay_memory_params,
        strategy_params,
        state_dim,
        action_dim,
        soft_target_update_rate=.001,
        actor_optimizer='adam',
        actor_optimizer_params={'learning_rate': 0.0001},
        critic_optimizer='adam',
        critic_optimizer_params={'learning_rate': 0.001},
        ctx=None,
        discount_factor=.9,
        training_episodes=50,
        start_training=20,
        train_interval=1,
        snapshot_interval=200,
        agent_name='DdpgAgent',
        max_episode_step=9999,
        evaluation_samples=100,
        self_play='no',
        output_directory='model_parameters',
        verbose=True,
        target_score=None,
        policy_noise=0.2,
        noise_clip=0.5,
        policy_delay=2
    ):
        super(TwinDelayedDdpgAgent, self).__init__(
            environment=environment, replay_memory_params=replay_memory_params,
            strategy_params=strategy_params, state_dim=state_dim,
            action_dim=action_dim, ctx=ctx, discount_factor=discount_factor,
            training_episodes=training_episodes, start_training=start_training,
            train_interval=train_interval,
            snapshot_interval=snapshot_interval, agent_name=agent_name,
            max_episode_step=max_episode_step,
            output_directory=output_directory, verbose=verbose,
            target_score=target_score, evaluation_samples=evaluation_samples,
            self_play=self_play,
            critic=critic, soft_target_update_rate=soft_target_update_rate,
            actor=actor, actor_optimizer=actor_optimizer,
            actor_optimizer_params=actor_optimizer_params,
            critic_optimizer=critic_optimizer,
            critic_optimizer_params=critic_optimizer_params)

        self._policy_noise = policy_noise
        self._noise_clip = noise_clip
        self._policy_delay = policy_delay

        self._critic2 = self._critic.__class__()
        self._critic2.collect_params().initialize(
            mx.init.Normal(), ctx=self._ctx)
        self._critic2.hybridize()
        self._critic2(nd.ones((1,) + state_dim, ctx=self._ctx),
                      nd.ones((1,) + action_dim, ctx=self._ctx))

        self._critic2_target = self._copy_critic2()

        self._critic2_optimizer = critic_optimizer
        self._critic2_optimizer_params = self._adjust_optimizer_params(
            critic_optimizer_params)

    def _make_pickle_ready(self, session_dir):
        super(TwinDelayedDdpgAgent, self)._make_pickle_ready(session_dir)
        self._export_net(self._critic2, 'critic2', filedir=session_dir)
        self._critic2 = None
        self._export_net(
            self._critic2_target, 'critic2_target', filedir=session_dir)
        self._critic2_target = None

    @classmethod
    def resume_from_session(cls, session_dir, actor, critic, environment):
        import pickle
        if not os.path.exists(session_dir):
            raise ValueError('Session directory does not exist')

        files = dict()
        files['agent'] = os.path.join(session_dir, 'agent.p')
        files['best_net_params'] = os.path.join(session_dir, 'best_net.params')
        files['actor_net_params'] = os.path.join(session_dir, 'actor.params')
        files['actor_target_net_params'] = os.path.join(
            session_dir, 'actor_target.params')
        files['critic_net_params'] = os.path.join(session_dir, 'critic.params')
        files['critic_target_net_params'] = os.path.join(
            session_dir, 'critic_target.params')
        files['critic2_net_params'] = os.path.join(
            session_dir, 'critic2.params')
        files['critic2_target_net_params'] = os.path.join(
            session_dir, 'critic2_target.params')

        for file in files.values():
            if not os.path.exists(file):
                raise ValueError(
                    'Session directory is not complete: {} is missing'
                    .format(file))

        with open(files['agent'], 'rb') as f:
            agent = pickle.load(f)

        agent._environment = environment

        agent._actor = actor
        agent._actor.load_parameters(files['actor_net_params'], agent._ctx)
        agent._actor.hybridize()
        agent._actor(nd.random_normal(
            shape=((1,) + agent._state_dim), ctx=agent._ctx))

        agent._best_net = copy_net(agent._actor, agent._state_dim, agent._ctx)
        agent._best_net.load_parameters(files['best_net_params'], agent._ctx)

        agent._actor_target = copy_net(
            agent._actor, agent._state_dim, agent._ctx)
        agent._actor_target.load_parameters(files['actor_target_net_params'])

        agent._critic = critic
        agent._critic.load_parameters(files['critic_net_params'], agent._ctx)
        agent._critic.hybridize()
        agent._critic(
            nd.random_normal(shape=((1,) + agent._state_dim), ctx=agent._ctx),
            nd.random_normal(shape=((1,) + agent._action_dim), ctx=agent._ctx))

        agent._critic_target = copy_net_with_two_inputs(
            agent._critic, agent._state_dim, agent._action_dim, agent._ctx)
        agent._critic_target.load_parameters(files['critic_target_net_params'])

        agent._critic2 = copy_net_with_two_inputs(
            agent._critic, agent._state_dim, agent._action_dim, agent._ctx)
        agent._critic2.load_parameters(files['critic2_net_params'], agent._ctx)
        agent._critic2.hybridize()
        agent._critic2(
            nd.random_normal(shape=((1,) + agent._state_dim), ctx=agent._ctx),
            nd.random_normal(shape=((1,) + agent._action_dim), ctx=agent._ctx))

        agent._critic2_target = copy_net_with_two_inputs(
            agent._critic2, agent._state_dim, agent._action_dim, agent._ctx)
        agent._critic2_target.load_parameters(
            files['critic2_target_net_params'])

        agent._logger = ArchLogger.get_logger()
        agent._training_stats.logger = ArchLogger.get_logger()
        agent._logger.info('Agent was retrieved; Training can be continued')

        return agent

    def _copy_critic2(self):
        assert self._critic2 is not None
        assert self._ctx is not None
        assert type(self._state_dim) is tuple
        assert type(self._action_dim) is tuple
        return copy_net_with_two_inputs(
            self._critic2, self._state_dim, self._action_dim, ctx=self._ctx)

    def train(self, episodes=None):
        self.save_config_file()
        self._logger.info("--- Start TwinDelayedDDPG training ---")
        episodes = \
            episodes if episodes is not None else self._training_episodes

        resume = (self._current_episode > 0)
        if resume:
            self._logger.info("Training session resumed")
            self._logger.info(
                "Starting from episode {}".format(self._current_episode))
        else:
            self._training_stats = DdpgTrainingStats(episodes)

            # Initialize target Q1' and Q2' and mu'
            self._actor_target = self._copy_actor()
            self._critic_target = self._copy_critic()
            self._critic2_target = self._copy_critic2()

        # Initialize l2 loss for critic network
        l2_loss = gluon.loss.L2Loss()

        # Initialize critic and actor trainer
        trainer_actor = gluon.Trainer(
            self._actor.collect_params(), self._actor_optimizer,
            self._actor_optimizer_params)
        trainer_critic = gluon.Trainer(
            self._critic.collect_params(), self._critic_optimizer,
            self._critic_optimizer_params)
        trainer_critic2 = gluon.Trainer(
            self._critic2.collect_params(), self._critic2_optimizer,
            self._critic2_optimizer_params)

        # For episode=1..n
        while self._current_episode < episodes:
            # Check interrupt flag
            if self._check_interrupt_routine():
                return False

            # Initialize new episode
            step = 0
            episode_reward = 0
            start = time.time()
            episode_critic_loss = 0
            episode_actor_loss = 0
            episode_avg_q_value = 0
            training_steps = 0
            actor_updates = 0

            # Get initialial observation state s
            state = self._environment.reset()

            # For step=1..T
            while step < self._max_episode_step:
                # Select an action a = mu(s) + N(step) according to current
                #  actor and exploration noise N according to strategy
                action = self._strategy.select_action(
                    self.get_next_action(state))
                self._strategy.decay(self._current_episode)

                # Execute action a and observe reward r and next state ns
                next_state, reward, terminal, _ = \
                    self._environment.step(action)

                self._logger.debug(
                    'Applied action {} with reward {}'.format(action, reward))

                # Store transition (s,a,r,ns) in replay buffer
                self._memory.append(
                    state, action, reward, next_state, terminal)

                if self._do_training():
                    # Sample random minibatch of b transitions
                    # (s_i, a_i, r_i, s_(i+1)) from replay buffer
                    states, actions, rewards, next_states, terminals =\
                         self._sample_from_memory()

                    clipped_noise = nd.array(
                        np.clip(
                            np.random.normal(
                                loc=0, scale=self._policy_noise,
                                size=self._minibatch_size
                            ).reshape(self._minibatch_size, 1),
                            -self._noise_clip,
                            self._noise_clip
                        ),
                        ctx=self._ctx
                    )
                    target_action = np.clip(
                        self._actor_target(next_states)[0][0] + clipped_noise,
                        self._strategy._action_low,
                        self._strategy._action_high)

                    rewards = rewards.reshape(self._minibatch_size, 1)
                    terminals = terminals.reshape(self._minibatch_size, 1)

                    target_qvalues1 = self._critic_target(next_states,
                                                          target_action)[0][0]
                    target_qvalues2 = self._critic2_target(next_states,
                                                           target_action)[0][0]
                    target_qvalues = nd.minimum(target_qvalues1,
                                                target_qvalues2)
                    y = rewards + (1 - terminals) * self._discount_factor\
                        * target_qvalues

                    with autograd.record():
                        qvalues1 = self._critic(states, actions)
                        critic1_loss = l2_loss(qvalues1[0][0], y)
                    critic1_loss.backward()
                    trainer_critic.step(self._minibatch_size)

                    with autograd.record():
                        qvalues2 = self._critic2(states, actions)
                        critic2_loss = l2_loss(qvalues2[0][0], y)
                    critic2_loss.backward()
                    trainer_critic2.step(self._minibatch_size)

                    critic_loss = (critic1_loss.mean() + critic2_loss.mean())/2

                    if self._total_steps % self._policy_delay == 0:
                        tmp_critic = self._copy_critic()
                        with autograd.record():
                            actor_loss = -tmp_critic(
                                states, self._actor(states)[0][0])[0][0].mean()
                        actor_loss.backward()
                        trainer_actor.step(self._minibatch_size)

                        # Update target networks:
                        self._actor_target = self._soft_update(
                            self._actor, self._actor_target,
                            self._soft_target_update_rate)
                        self._critic_target = self._soft_update(
                            self._critic, self._critic_target,
                            self._soft_target_update_rate)
                        self._critic2_target = self._soft_update(
                            self._critic2, self._critic2_target,
                            self._soft_target_update_rate)

                        actor_updates = actor_updates + 1
                    else:
                        actor_loss = nd.array([0], ctx=self._ctx)

                    # Update statistics
                    episode_critic_loss +=\
                        np.sum(critic_loss.asnumpy()) / self._minibatch_size
                    episode_actor_loss += 0 if actor_updates == 0 else\
                        np.sum(actor_loss.asnumpy()[0])
                    episode_avg_q_value +=\
                        np.sum(target_qvalues.asnumpy()) / self._minibatch_size

                    training_steps += 1

                episode_reward += reward
                step += 1
                self._total_steps += 1
                state = next_state

                if terminal:
                    # Reset the strategy
                    self._strategy.reset()
                    break

            # Log the episode results
            episode_actor_loss = 0 if actor_updates == 0\
                else (episode_actor_loss / actor_updates)
            episode_critic_loss = 0 if training_steps == 0\
                else (episode_critic_loss / training_steps)
            episode_avg_q_value = 0 if actor_updates == 0\
                else (episode_avg_q_value / training_steps)

            avg_reward = self._training_stats.log_episode(
                self._current_episode, start, training_steps,
                episode_actor_loss, episode_critic_loss, episode_avg_q_value,
                self._strategy.cur_eps, episode_reward)

            self._do_snapshot_if_in_interval(self._current_episode)

            if self._is_target_reached(avg_reward):
                self._logger.info(
                    'Target score is reached in average; Training is stopped')
                break

            self._current_episode += 1

        self._evaluate()
        self.save_parameters(episode=self._current_episode)
        self.export_best_network()
        self._training_stats.save_stats(self._output_directory)
        self._logger.info('--------- Training finished ---------')
        return True

        def _make_config_dict(self):
            config = super(TwinDelayedDdpgAgent, self)._make_config_dict()
            config['policy_noise'] = self._policy_noise
            config['noise_clip'] = self._noise_clip
            config['policy_delay'] = self._policy_delay
            return config


class DqnAgent(Agent):
    def __init__(
        self,
        qnet,
        environment,
        replay_memory_params,
        strategy_params,
        state_dim,
        action_dim,
        ctx=None,
        discount_factor=.9,
        loss_function='l2',
        optimizer='rmsprop',
        optimizer_params={'learning_rate': 0.09},
        training_episodes=50,
        start_training=0,
        train_interval=1,
        use_fix_target=False,
        double_dqn=False,
        target_update_interval=10,
        snapshot_interval=200,
        evaluation_samples=100,
        self_play='no',
        agent_name='Dqn_agent',
        max_episode_step=99999,
        output_directory='model_parameters',
        verbose=True,
        target_score=None
    ):
        super(DqnAgent, self).__init__(
            environment=environment, replay_memory_params=replay_memory_params,
            strategy_params=strategy_params, state_dim=state_dim,
            action_dim=action_dim, ctx=ctx, discount_factor=discount_factor,
            training_episodes=training_episodes, start_training=start_training,
            train_interval=train_interval,
            snapshot_interval=snapshot_interval, agent_name=agent_name,
            max_episode_step=max_episode_step,
            output_directory=output_directory, verbose=verbose,
            target_score=target_score, evaluation_samples=evaluation_samples,
            self_play=self_play)

        self._qnet = qnet
        self._target_update_interval = target_update_interval
        self._target_qnet = copy_net(
            self._qnet, self._state_dim, ctx=self._ctx)
        self._loss_function_str = loss_function
        self._loss_function = get_loss_function(loss_function)
        self._optimizer = optimizer
        self._optimizer_params = optimizer_params
        self._double_dqn = double_dqn
        self._use_fix_target = use_fix_target

        # Build memory buffer for discrete actions
        replay_memory_params['state_dim'] = state_dim
        replay_memory_params['action_dim'] = (1,)
        self._replay_memory_params = replay_memory_params
        rm_builder = ReplayMemoryBuilder()
        self._memory = rm_builder.build_by_params(**replay_memory_params)
        self._minibatch_size = self._memory.sample_size

        # Initialize best network
        self._best_net = copy_net(self._qnet, self._state_dim, self._ctx)
        self._best_avg_score = -np.infty

        self._training_stats = None

    @classmethod
    def resume_from_session(cls, session_dir, net, environment):
        import pickle
        if not os.path.exists(session_dir):
            raise ValueError('Session directory does not exist')

        files = dict()
        files['agent'] = os.path.join(session_dir, 'agent.p')
        files['best_net_params'] = os.path.join(session_dir, 'best_net.params')
        files['q_net_params'] = os.path.join(session_dir, 'qnet.params')
        files['target_net_params'] = os.path.join(
            session_dir, 'target_net.params')

        for file in files.values():
            if not os.path.exists(file):
                raise ValueError(
                    'Session directory is not complete: {} is missing'
                    .format(file))

        with open(files['agent'], 'rb') as f:
            agent = pickle.load(f)

        agent._environment = environment
        agent._qnet = net
        agent._qnet.load_parameters(files['q_net_params'], agent._ctx)
        agent._qnet.hybridize()
        agent._qnet(nd.random_normal(
            shape=((1,) + agent._state_dim), ctx=agent._ctx))
        agent._best_net = copy_net(agent._qnet, agent._state_dim, agent._ctx)
        agent._best_net.load_parameters(files['best_net_params'], agent._ctx)
        agent._target_qnet = copy_net(
            agent._qnet, agent._state_dim, agent._ctx)
        agent._target_qnet.load_parameters(
            files['target_net_params'], agent._ctx)

        agent._logger = ArchLogger.get_logger()
        agent._training_stats.logger = agent._logger
        agent._logger.info('Agent was retrieved; Training can be continued')

        return agent

    def _make_pickle_ready(self, session_dir):
        super(DqnAgent, self)._make_pickle_ready(session_dir)
        self._export_net(self._qnet, 'current_qnet')
        self._export_net(self._qnet, 'qnet', filedir=session_dir)
        self._qnet = None
        self._export_net(self._target_qnet, 'target_net', filedir=session_dir)
        self._target_qnet = None

    def get_q_values(self, state, with_best=False):
        return self.get_batch_q_values(
            nd.array([state], ctx=self._ctx), with_best=with_best)[0]

    def get_batch_q_values(self, state_batch, with_best=False):
        return self._best_net(state_batch)\
            if with_best else self._qnet(state_batch)

    def get_next_action(self, state, with_best=False):
        q_values = self.get_q_values(state, with_best=with_best)
        action = q_values[0][0].asnumpy().argmax()
        return action

    def __determine_target_q_values(
        self, states, actions, rewards, next_states, terminals
    ):
        if self._use_fix_target:
            q_max_val = self._target_qnet(next_states)[0][0]
        else:
            q_max_val = self._qnet(next_states)[0][0]

        if self._double_dqn:
            q_values_next_states = self._qnet(next_states)[0][0]
            target_rewards = rewards + nd.choose_element_0index(
                q_max_val, nd.argmax_channel(q_values_next_states))\
                * (1.0 - terminals) * self._discount_factor
        else:
            target_rewards = rewards + nd.choose_element_0index(
                q_max_val, nd.argmax_channel(q_max_val))\
                * (1.0 - terminals) * self._discount_factor

        actions = actions.astype(int)

        target_qval = self._qnet(states)
        for t in range(target_rewards.shape[0]):
            target_qval[0][0][t][actions.asnumpy()[t, 0]] = target_rewards[t]

        return target_qval[0][0]

    def __train_q_net_step(self, trainer):
        states, actions, rewards, next_states, terminals =\
            self._sample_from_memory()
        target_qval = self.__determine_target_q_values(
            states, actions, rewards, next_states, terminals)
        with autograd.record():
            q_values = self._qnet(states)
            loss = self._loss_function(q_values[0][0], target_qval)
        loss.backward()
        trainer.step(self._minibatch_size)
        return loss

    def __do_target_update_if_in_interval(self, total_steps):
        do_target_update = (
            self._use_fix_target and
            (total_steps % self._target_update_interval == 0))
        if do_target_update:
            self._logger.info(
                'Target network is updated after {} steps'.format(total_steps))
            self._target_qnet = copy_net(
                self._qnet, self._state_dim, self._ctx)

    def train(self, episodes=None):
        self.save_config_file()
        self._logger.info("--- Start training ---")
        trainer = gluon.Trainer(
            self._qnet.collect_params(),
            self._optimizer,
            self._adjust_optimizer_params(self._optimizer_params))
        episodes = episodes if episodes is not None\
            else self._training_episodes

        resume = (self._current_episode > 0)
        if resume:
            self._logger.info("Training session resumed")
            self._logger.info("Starting from episode {}".format(
                self._current_episode))
        else:
            self._training_stats = DqnTrainingStats(episodes)

        # Implementation Deep Q Learning described by
        # Mnih et. al. in Playing Atari with Deep Reinforcement Learning
        while self._current_episode < episodes:
            if self._check_interrupt_routine():
                return False

            step = 0
            episode_reward = 0
            start = time.time()
            state = self._environment.reset()
            episode_loss = 0
            training_steps = 0
            while step < self._max_episode_step:
                # 1. Choose an action based on current game state and policy
                q_values = self._qnet(nd.array([state], ctx=self._ctx))
                action = self._strategy.select_action(q_values[0])
                self._strategy.decay(self._current_episode)

                # 2. Play the game for a single step
                next_state, reward, terminal, _ =\
                    self._environment.step(action)

                # 3. Store transition in replay memory
                self._memory.append(
                    state, [action], reward, next_state, terminal)

                # 4. Train the network if in interval
                if self._do_training():
                    loss = self.__train_q_net_step(trainer)
                    training_steps += 1
                    episode_loss +=\
                        np.sum(loss.asnumpy()) / self._minibatch_size

                # Update target network if in interval
                self.__do_target_update_if_in_interval(self._total_steps)

                step += 1
                self._total_steps += 1
                episode_reward += reward
                state = next_state

                if terminal:
                    self._strategy.reset()
                    break

            self._do_snapshot_if_in_interval(self._current_episode)

            episode_loss = (episode_loss / training_steps)\
                if training_steps > 0 else 0
            avg_reward = self._training_stats.log_episode(
                self._current_episode, start, training_steps,
                episode_loss, self._strategy.cur_eps, episode_reward)

            if self._is_target_reached(avg_reward):
                self._logger.info(
                    'Target score is reached in average; Training is stopped')
                break

            self._current_episode += 1

        self._evaluate()
        self.save_parameters(episode=self._current_episode)
        self.export_best_network()
        self._training_stats.save_stats(self._output_directory)
        self._logger.info('--------- Training finished ---------')
        return True

    def _make_config_dict(self):
        config = super(DqnAgent, self)._make_config_dict()
        config['optimizer'] = self._optimizer
        config['optimizer_params'] = self._optimizer_params
        config['loss_function'] = self._loss_function_str
        config['use_fix_target'] = self._use_fix_target
        config['double_dqn'] = self._double_dqn
        config['target_update_interval'] = self._target_update_interval
        return config

    def save_parameters(self, episode):
        self._export_net(
            self._qnet, self._agent_name + '_qnet', episode=episode)

    def _save_current_as_best_net(self):
        self._best_net = copy_net(
            self._qnet, self._state_dim, ctx=self._ctx)
