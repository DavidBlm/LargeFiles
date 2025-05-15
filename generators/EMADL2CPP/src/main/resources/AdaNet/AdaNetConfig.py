from enum import Enum


class AdaNetConfig(Enum):
    MAX_NUM_ROUNDS = 100
    GAMMA = .1
    ALPHA = .07
    BETA = .001
    LAMBDA = 0.01
    DEFAULT_BLOCK = 'FullyConnected'
    DEFAULT_BLOCK_ARGS = {'units': 20,
                          'activation': 'relu',
                          'flatten': True}
