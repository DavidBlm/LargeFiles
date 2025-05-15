"""
    this module provides the abstract classes as well as concrete classes for the AdaNet algorithm components
"""
from abc import ABC, abstractmethod
from typing import Tuple, Dict, List
import mxnet.gluon
import numpy as np
import mxnet.gluon.nn as nn
from mxnet.ndarray import zeros
from adaNetUtils import train_candidate
from AdaNetConfig import AdaNetConfig


class DataClass:
    """
        this object holds all the necessary information for AdaNet
    """

    def __init__(self, outBlock: mxnet.gluon.HybridBlock, inBlock: mxnet.gluon.HybridBlock,
                 block: mxnet.gluon.HybridBlock, model_shape: Tuple[int], **kwargs):
        FullyConnected = AdaNetConfig.DEFAULT_BLOCK.value
        self.inBlock = inBlock
        self.block = block
        if self.block is AdaNetConfig.DEFAULT_BLOCK.value:
            self.block = DefaultBuildingBlock
        self.outBlock = outBlock
        self.model_shape = model_shape


class SuperBuildingBlock(ABC, mxnet.gluon.HybridBlock):
    """
        the essential building block which gets stacked
    """

    def __init__(self, operation: mxnet.gluon.HybridBlock, **kwargs):
        super(SuperBuildingBlock, self).__init__(**kwargs)
        self.node_count = None
        self.operation = operation()  # it is expected that the block doesn't take any parameters
        self.oc = None

    def get_emadl_repr(self) -> str:
        if isinstance(self.operation, DefaultBuildingBlock):
            res = self.operation.get_emadl_repr()
        else:
            res = type(self.operation).__name__

        return res

    def count_nodes(self):
        if self.node_count is None:
            oc = 0
            params = self.collect_params()
            for el in params:
                if 'bias' in el:
                    # each node in a layer has a bias parameter
                    # => number of nodes = number of bias
                    oc += params[el].shape[0]
            self.node_count = oc
        return self.node_count

    def hybrid_forward(self, F, x, *args, **kwargs):
        return self.operation(x)


class DefaultBuildingBlock(mxnet.gluon.HybridBlock):
    def __init__(self, **kwargs):
        super(DefaultBuildingBlock, self).__init__(**kwargs)
        with self.name_scope():
            self.operation = nn.Dense(**AdaNetConfig.DEFAULT_BLOCK_ARGS.value)

    def get_emadl_repr(self) -> str:
        return f"{AdaNetConfig.DEFAULT_BLOCK.value}(units={AdaNetConfig.DEFAULT_BLOCK_ARGS.value['units']})->\nRelu"

    def hybrid_forward(self, F, x, *args, **kwargs):
        return self.operation(x)


class SuperCandidateHull(ABC, mxnet.gluon.HybridBlock):
    """
        this is a wrapper to resemble a ensemble candidate
    """

    def __init__(self, building_block: mxnet.gluon.HybridBlock, name: str, model_shape: Tuple[int],
                 rade_aprox: callable = np.sqrt,
                 in_block: mxnet.gluon.HybridBlock = None,
                 out_block: mxnet.gluon.HybridBlock = None, **kwargs):
        super(SuperCandidateHull, self).__init__(**kwargs)
        with self.name_scope():
            self.building_block = building_block
            self.name_ = name
            self.rade_aprox = rade_aprox
            self.node_count = None
            self.complexity = None
            self.rade = None
            self.model_shape = model_shape

            if in_block is not None:
                self.input = in_block()
            else:
                self.input = None

            if out_block is not None:
                self.output = out_block()
            else:
                self.output = None

            self.build()
            self.units = int(np.prod(self.model_shape))
            self.finalOut = nn.Dense(units=self.units, flatten=False)

    def get_complexity(self) -> float:
        """
            getter for complexity score

            returns the complexity calculated by the training loss function and is updated there
            (alpha * r + beta)* ||w||_L1
            r = approximation of the rademacher complexity
        """
        return self.complexity

    def update_complexity(self, complexity) -> None:
        """
            setter for  complexity score
        """
        self.complexity = complexity

    def approximate_rade(self) -> float:
        """
            calculates the rademacher approximation based on the self.rade_aprox
        """
        return self.rade_aprox(self.count_nodes())

    @abstractmethod
    def get_emadl_repr(self) -> str:

        raise NotImplementedError(f"this function has to be fitted for your builder design, the {SuperBuildingBlock}"
                                  f"provides the function get_emadl_repr "
                                  f"it returns the name of the generated HybridBlock"
                                  f"its name is the same as in the emadl file")

    @abstractmethod
    def build(self) -> None:
        """
            this function builds the model
        """
        raise NotImplementedError("No build function defined, you have to design your own building "
                                  "function which builds your candidates according your chosen strategy")

    @abstractmethod
    def count_nodes(self) -> int:
        """
            counts the nodes in this candidate
            should call count_nodes() of its building_block properties
        """
        raise NotImplementedError(
            f" this function needs to be applied for your model design, use the {SuperBuildingBlock} "
            f"function count_nodes to get the number of nodes within a =BuildingBlock ")


class SuperBuilder(ABC):
    def __init__(self, batch_size: int,
                 model_shape: Tuple[int],
                 loss: mxnet.gluon.loss.Loss,
                 build_operation: mxnet.gluon.HybridBlock,
                 train_iterator: mxnet.io.NDArrayIter,
                 epochs: int,
                 optimizer: str,
                 optimizer_params: dict,
                 ctx: mxnet.context.Context,
                 in_block: mxnet.gluon.HybridBlock = None,
                 out_block: mxnet.gluon.HybridBlock = None,
                 **kwargs):
        self.batch_size = batch_size
        self.model_shape = model_shape
        self.build_operation = build_operation
        self.input = in_block
        self.output = out_block
        self.train_iterator = train_iterator
        self.loss = loss
        self.ctx = ctx
        self.epochs = epochs
        self.optimizer = optimizer
        self.optimizer_params = optimizer_params

    def train(self, candidate) -> List[float]:
        candidate.initialize(ctx=self.ctx)
        candidate.hybridize()
        candidate_loss = train_candidate(candidate, epochs=self.epochs, optimizer=self.optimizer,
                                         optimizer_params=self.optimizer_params, loss=self.loss,
                                         train_data_iterator=self.train_iterator)
        return candidate_loss

    @abstractmethod
    def get_candidates(self) -> Dict[str, Tuple[SuperCandidateHull, List[float]]]:
        """
            this function returns a dictionary containing the trained candidates
            key: name of the candidate
            data: tuple of size 2,
                (candidate,training_loss)
        """
        raise NotImplementedError

    @abstractmethod
    def update(self, up=1) -> None:
        """
            :param up function to update the builder for its next round
        """
        raise NotImplementedError


class SuperModelTemplate(ABC, mxnet.gluon.HybridBlock):
    def __init__(self, operations: Dict[str, SuperCandidateHull], batch_size: int, model_shape: Tuple[int],
                 generation: bool = True, **kwargs):
        super(SuperModelTemplate, self).__init__(**kwargs)
        with self.name_scope():
            self.operations = operations
            self.AdaNet = True
            self.op_names = []
            self.generation = generation
            self.candidate_complexities = {}
            self.batch_size = batch_size
            self.model_shape = model_shape
            self.units = int(np.prod(self.model_shape))
            self.build()
            self.out = nn.Dense(units=self.units, flatten=True)
        pass

    def get_candidate_complexity(self):
        mean_complexity = zeros(len(self.op_names))
        for i, name in enumerate(self.op_names):
            mean_complexity[i] = self.candidate_complexities[name]
        return mean_complexity

    @abstractmethod
    def build(self) -> None:
        raise NotImplementedError('this function builds your model is called by this superclass')

    @abstractmethod
    def get_emadl_repr(self) -> str:
        raise NotImplementedError('fit this function to your building strategy')
