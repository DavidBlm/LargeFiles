# (c) https://github.com/MontiCore/monticore
import numpy as np


class ReplayMemoryBuilder(object):
    def __init__(self):
        self.__supported_methods = ['online', 'buffer', 'combined']

    def build_by_params(
        self,
        state_dim,
        method='online',
        state_dtype='float32',
        action_dim=(1,),
        action_dtype='uint8',
        rewards_dtype='float32',
        memory_size=1000,
        sample_size=32
    ):
        assert state_dim is not None
        assert action_dim is not None
        assert method in self.__supported_methods

        if method == 'online':
            return self.build_online_memory(
                state_dim=state_dim, state_dtype=state_dtype,
                action_dtype=action_dtype, action_dim=action_dim,
                rewards_dtype=rewards_dtype)
        else:
            assert memory_size is not None and memory_size > 0
            assert sample_size is not None and sample_size > 0
            if method == 'buffer':
                return self.build_buffered_memory(
                    state_dim=state_dim, sample_size=sample_size,
                    memory_size=memory_size, state_dtype=state_dtype,
                    action_dim=action_dim, action_dtype=action_dtype,
                    rewards_dtype=rewards_dtype)
            else:
                return self.build_combined_memory(
                    state_dim=state_dim, sample_size=sample_size,
                    memory_size=memory_size, state_dtype=state_dtype,
                    action_dim=action_dim, action_dtype=action_dtype,
                    rewards_dtype=rewards_dtype)

    def build_buffered_memory(
        self, state_dim, memory_size, sample_size, state_dtype, action_dim,
        action_dtype, rewards_dtype
    ):
        assert memory_size > 0
        assert sample_size > 0
        return ReplayMemory(
            state_dim, size=memory_size, sample_size=sample_size,
            state_dtype=state_dtype, action_dim=action_dim,
            action_dtype=action_dtype, rewards_dtype=rewards_dtype)

    def build_combined_memory(
        self, state_dim, memory_size, sample_size, state_dtype, action_dim,
        action_dtype, rewards_dtype
    ):
        assert memory_size > 0
        assert sample_size > 0
        return CombinedReplayMemory(
            state_dim, size=memory_size, sample_size=sample_size,
            state_dtype=state_dtype, action_dim=action_dim,
            action_dtype=action_dtype, rewards_dtype=rewards_dtype)

    def build_online_memory(
        self, state_dim, state_dtype, action_dtype, action_dim, rewards_dtype
    ):
        return OnlineReplayMemory(
            state_dim, state_dtype=state_dtype, action_dim=action_dim,
            action_dtype=action_dtype, rewards_dtype=rewards_dtype)


class ReplayMemory(object):
    def __init__(
        self,
        state_dim,
        sample_size,
        size=1000,
        action_dim=(1,),
        state_dtype='float32',
        action_dtype='uint8',
        rewards_dtype='float32'
    ):
        assert size > 0, "Size must be greater than zero"
        assert type(state_dim) is tuple, "State dimension must be a tuple"
        assert type(action_dim) is tuple, "Action dimension must be a tuple"
        assert sample_size > 0
        self._size = size
        self._sample_size = sample_size
        self._cur_size = 0
        self._pointer = 0
        self._state_dim = state_dim
        self._state_dtype = state_dtype
        self._action_dim = action_dim
        self._action_dtype = action_dtype
        self._rewards_dtype = rewards_dtype
        self._states = np.zeros((self._size,) + state_dim, dtype=state_dtype)
        self._actions = np.zeros(
            (self._size,) + action_dim, dtype=action_dtype)
        self._rewards = np.array([0] * self._size, dtype=rewards_dtype)
        self._next_states = np.zeros(
            (self._size,) + state_dim, dtype=state_dtype)
        self._terminals = np.array([0] * self._size, dtype='bool')

    @property
    def sample_size(self):
        return self._sample_size

    def append(self, state, action, reward, next_state, terminal):
        self._states[self._pointer] = state
        self._actions[self._pointer] = action
        self._rewards[self._pointer] = reward
        self._next_states[self._pointer] = next_state
        self._terminals[self._pointer] = terminal

        self._pointer = self._pointer + 1
        if self._pointer == self._size:
            self._pointer = 0

        self._cur_size = min(self._size, self._cur_size + 1)

    def at(self, index):
        return self._states[index],\
            self._actions[index],\
            self._rewards[index],\
            self._next_states[index],\
            self._terminals[index]

    def is_sample_possible(self, batch_size=None):
        batch_size = batch_size if batch_size is not None\
            else self._sample_size
        return self._cur_size >= batch_size

    def sample(self, batch_size=None):
        batch_size = batch_size if batch_size is not None\
            else self._sample_size
        assert self._cur_size >= batch_size,\
            "Size of replay memory must be larger than batch size"
        i = 0
        states = np.zeros((
            batch_size,)+self._state_dim, dtype=self._state_dtype)
        actions = np.zeros(
            (batch_size,)+self._action_dim,  dtype=self._action_dtype)
        rewards = np.zeros(batch_size, dtype=self._rewards_dtype)
        next_states = np.zeros(
            (batch_size,)+self._state_dim, dtype=self._state_dtype)
        terminals = np.zeros(batch_size, dtype='bool')

        while i < batch_size:
            rnd_index = np.random.randint(low=0, high=self._cur_size)
            states[i] = self._states.take(rnd_index, axis=0)
            actions[i] = self._actions.take(rnd_index, axis=0)
            rewards[i] = self._rewards.take(rnd_index, axis=0)
            next_states[i] = self._next_states.take(rnd_index, axis=0)
            terminals[i] = self._terminals.take(rnd_index, axis=0)
            i += 1

        return states, actions, rewards, next_states, terminals


class OnlineReplayMemory(ReplayMemory):
    def __init__(
        self, state_dim, state_dtype='float32', action_dim=(1,),
        action_dtype='uint8', rewards_dtype='float32'
    ):
        super(OnlineReplayMemory, self).__init__(
            state_dim, sample_size=1, size=1, state_dtype=state_dtype,
            action_dim=action_dim, action_dtype=action_dtype,
            rewards_dtype=rewards_dtype)


class CombinedReplayMemory(ReplayMemory):
    def __init__(
        self, state_dim, sample_size, size=1000, state_dtype='float32',
        action_dim=(1,), action_dtype='uint8', rewards_dtype='float32'
    ):
        super(CombinedReplayMemory, self).__init__(
            state_dim=state_dim, sample_size=(sample_size - 1), size=size,
            state_dtype=state_dtype, action_dim=action_dim,
            action_dtype=action_dtype, rewards_dtype=rewards_dtype)

        self._last_state = np.zeros((1,) + state_dim, dtype=state_dtype)
        self._last_action = np.array((1,) + action_dim, dtype=action_dtype)
        self._last_reward = np.array([0], dtype=rewards_dtype)
        self._last_next_state = np.zeros((1,) + state_dim, dtype=state_dtype)
        self._last_terminal = np.array([0], dtype='bool')

    def append(self, state, action, reward, next_state, terminal):
        super(CombinedReplayMemory, self).append(
            state, action, reward, next_state, terminal)
        self._last_state = state
        self._last_action = action
        self._last_reward = reward
        self._last_next_state = next_state
        self._last_terminal = terminal

    def sample(self, batch_size=None):
        batch_size = (batch_size-1) if batch_size is not None\
            else self._sample_size
        states, actions, rewards, next_states, terminals = super(
            CombinedReplayMemory, self).sample(batch_size=batch_size)
        states = np.append(states, [self._last_state], axis=0)
        actions = np.append(actions, [self._last_action], axis=0)
        rewards = np.append(rewards, [self._last_reward], axis=0)
        next_states = np.append(next_states, [self._last_next_state], axis=0)
        terminals = np.append(terminals, [self._last_terminal], axis=0)
        return states, actions, rewards, next_states, terminals
