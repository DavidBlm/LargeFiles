# (c) https://github.com/MontiCore/monticore
import numpy as np


class StrategyBuilder(object):
    def __init__(self):
        pass

    def build_by_params(
        self,
        method='epsgreedy',
        epsilon=0.5,
        min_epsilon=0.05,
        epsilon_decay_method='no',
        epsilon_decay=0.0,
        epsilon_decay_start=0,
        epsilon_decay_per_step=False,
        action_dim=None,
        action_low=None,
        action_high=None,
        mu=0.0,
        theta=0.5,
        sigma=0.3,
        noise_variance=0.1
    ):

        if epsilon_decay_method == 'linear':
            decay = LinearDecay(
                eps_decay=epsilon_decay, min_eps=min_epsilon,
                decay_start=epsilon_decay_start,
                decay_per_step=epsilon_decay_per_step)
        else:
            decay = NoDecay()

        if method == 'epsgreedy':
            assert action_dim is not None
            assert len(action_dim) == 1
            return EpsilonGreedyStrategy(
                eps=epsilon, number_of_actions=action_dim[0],
                decay_method=decay)
        elif method == 'ornstein_uhlenbeck':
            assert action_dim is not None
            assert action_low is not None
            assert action_high is not None
            assert mu is not None
            assert theta is not None
            assert sigma is not None
            return OrnsteinUhlenbeckStrategy(
                action_dim, action_low, action_high, epsilon, mu, theta,
                sigma, decay)
        elif method == 'gaussian':
            assert action_dim is not None
            assert action_low is not None
            assert action_high is not None
            assert noise_variance is not None
            return GaussianNoiseStrategy(action_dim, action_low, action_high,
                                         epsilon, noise_variance, decay)
        else:
            assert action_dim is not None
            assert len(action_dim) == 1
            return GreedyStrategy()


class BaseDecay(object):
    def __init__(self):
        pass

    def decay(self, *args):
        raise NotImplementedError

    def __call__(self, *args):
        return self.decay(*args)


class NoDecay(BaseDecay):
    def __init__(self):
        super(NoDecay, self).__init__()

    def decay(self, cur_eps, episode):
        return cur_eps


class LinearDecay(BaseDecay):
    def __init__(self, eps_decay, min_eps=0, decay_start=0, decay_per_step=False):
        super(LinearDecay, self).__init__()
        self.eps_decay = eps_decay
        self.min_eps = min_eps
        self.decay_start = decay_start
        self.decay_per_step = decay_per_step
        self.last_episode = -1

    def do_decay(self, episode):
        if self.decay_per_step:
            do = (episode >= self.decay_start)
        else:
            do = ((self.last_episode != episode) and (episode >= self.decay_start))
        self.last_episode = episode
        return do

    def decay(self, cur_eps, episode):
        if self.do_decay(episode):
            return max(cur_eps - self.eps_decay, self.min_eps)
        else:
            return cur_eps


class BaseStrategy(object):
    def __init__(self, decay_method):
        self._decay_method = decay_method

    def select_action(self, values, decay_method):
        raise NotImplementedError

    def decay(self, episode):
        self.cur_eps = self._decay_method.decay(self.cur_eps, episode)

    def reset(self):
        pass


class EpsilonGreedyStrategy(BaseStrategy):
    def __init__(self, eps, number_of_actions, decay_method):
        super(EpsilonGreedyStrategy, self).__init__(decay_method)
        self.eps = eps
        self.cur_eps = eps
        self.__number_of_actions = number_of_actions

    def select_action(self, values):
        do_exploration = (np.random.rand() < self.cur_eps)
        if do_exploration:
            action = np.random.randint(low=0, high=self.__number_of_actions)
        else:
            action = values[0][0].asnumpy().argmax()
        return action


class GreedyStrategy(BaseStrategy):
    def __init__(self):
        super(GreedyStrategy, self).__init__(None)

    def select_action(self, values):
        return values.asnumpy().argmax()

    def decay(self):
        pass


class OrnsteinUhlenbeckStrategy(BaseStrategy):
    """
    Ornstein-Uhlenbeck process: dxt = theta * (mu - xt) * dt + sigma * dWt
    where Wt denotes the Wiener process.
    """
    def __init__(
        self,
        action_dim,
        action_low,
        action_high,
        eps,
        mu=0.0,
        theta=.15,
        sigma=.3,
        decay=NoDecay()
    ):
        super(OrnsteinUhlenbeckStrategy, self).__init__(decay)
        self.eps = eps
        self.cur_eps = eps

        self._decay_method = decay

        self._action_dim = action_dim
        self._action_low = action_low
        self._action_high = action_high

        self._mu = np.array(mu)
        self._theta = np.array(theta)
        self._sigma = np.array(sigma)

        self.state = np.ones(self._action_dim) * self._mu

    def _evolve_state(self):
        x = self.state
        dx = self._theta * (self._mu - x)\
            + self._sigma * np.random.randn(len(x))
        self.state = x + dx
        return self.state

    def reset(self):
        self.state = np.ones(self._action_dim) * self._mu

    def select_action(self, values):
        noise = self._evolve_state()
        action = (1.0 - self.cur_eps) * values + (self.cur_eps * noise)
        return np.clip(action, self._action_low, self._action_high)


class GaussianNoiseStrategy(BaseStrategy):
    def __init__(
        self,
        action_dim,
        action_low,
        action_high,
        eps,
        noise_variance,
        decay=NoDecay()
    ):
        super(GaussianNoiseStrategy, self).__init__(decay)
        self.eps = eps
        self.cur_eps = eps

        self._action_dim = action_dim
        self._action_low = action_low
        self._action_high = action_high

        self._noise_variance = noise_variance

    def select_action(self, values):
        noise = np.random.normal(loc=0.0, scale=self._noise_variance, size=self._action_dim)
        action = values + self.cur_eps * noise
        return np.clip(action, self._action_low, self._action_high)
