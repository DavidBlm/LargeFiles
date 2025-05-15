import CoreAdaNet
from typing import Dict, Tuple, List
import mxnet as mx


class CandidateHull(CoreAdaNet.SuperCandidateHull):
    """
        the hull which holds the stacked building blocks and is potentially added to the model
    """

    def __init__(self, stack: int, **kwargs):
        self.names = []
        self.stack = stack
        self.body: Dict[str, CoreAdaNet.SuperBuildingBlock] = {}
        super(CandidateHull, self).__init__(**kwargs)

    def build(self) -> None:
        """
            this function builds the candidate function structure and is called by its superclass
            if there are parameters needed for the construction make sure they are added before the superclasses
            constructor is called!
        """
        body = {self.name_ + f"{i}": CoreAdaNet.SuperBuildingBlock(operation=self.building_block) for i in
                range(self.stack)}
        for name, operation in body.items():
            self.__setattr__(name, operation)
            self.names.append(name)
        self.body = body

    def count_nodes(self) -> int:
        if self.node_count is None:
            oc = 0
            for name in self.names:
                oc += self.__getattribute__(name).count_nodes()
            self.node_count = oc
        return self.node_count

    def hybrid_forward(self, F, x, *args, **kwargs):
        if self.input:
            x = self.input(x)

        for name in self.names:
            x = self.__getattribute__(name)(x)

        if self.output:
            x = self.output(x)
        x = self.finalOut(x)

        return x

    def get_emadl_repr(self) -> str:
        """
            this function generates the emadl representation of this candidate model
        """
        emadl_str = ''
        if self.input:
            emadl_str += f'{type(self.input).__name__}()'

        block_emadl_string = self.body[self.names[0]].get_emadl_repr()

        if self.stack > 1:
            emadl_str += f'\n{block_emadl_string}(->={self.stack})'
        else:
            emadl_str += f'\n{block_emadl_string}()'
        if self.output:
            emadl_str += f'->\n{type(self.output).__name__}()'

        if emadl_str:
            emadl_str += f'->\nFullyConnected(units={self.units})'
        return emadl_str


class Builder(CoreAdaNet.SuperBuilder):
    """
        the object which generates the new candidates
    """

    def __init__(self, **kwargs):
        super(Builder, self).__init__(**kwargs)
        self.round = 0
        self.pre_stack = 1
        self.step = 0
        self.candidate_per_round = 2

    def get_candidates(self) -> Dict[str, Tuple[CandidateHull, List[float]]]:
        """
        :returns a dict of already trained
        """

        candidates = {}
        for i in range(self.candidate_per_round):
            name = f'candidate{i}round{self.round}'
            candidate = CandidateHull(name=name, in_block=self.input, out_block=self.output, stack=i + self.pre_stack,
                                      model_shape=self.model_shape, building_block=self.build_operation)
            candidate_loss = self.train(candidate)
            candidates.setdefault(name, (candidate, candidate_loss))

        return candidates

    def update(self, up=1) -> None:
        """
        :param up, increases the stack height by up
        :returns None

        updates the round count and increases the stack height by up
        """
        self.pre_stack += up
        self.round += 1


class ModelTemplate(CoreAdaNet.SuperModelTemplate, mx.gluon.HybridBlock):
    def __init__(self, **kwargs):
        super(ModelTemplate, self).__init__(**kwargs)

    def get_emadl_repr(self) -> str:
        """
            this function generates an EMADL representation of the generated model
            replace the AdaNet call in the .emadl file with the generated string
            to avoid restarting the model
        """
        emadl_str = ''
        candidate_strings: List[str] = []
        for name, candidate in self.operations.items():
            candidate_strings.append(candidate.get_emadl_repr())
        if len(candidate_strings) > 1:
            for candidate_str in candidate_strings:
                if emadl_str:
                    emadl_str += f'|{candidate_str}'
                else:
                    emadl_str += f'({candidate_str}'
            emadl_str += ')->\nConcatenate()'
        elif len(candidate_strings) == 1:
            emadl_str += candidate_strings[0]

        if emadl_str:  # this string should never be empty when this function gets called
            emadl_str += f'->\nFullyConnected(units={self.units})->'
        return emadl_str

    def build(self):
        """
            this function builds the actual model, is called by the superclasses constructor
        """
        if self.operations is not None:
            for name, operation in self.operations.items():
                self.__setattr__(name, operation)
                self.op_names.append(name)
                self.candidate_complexities[name] = operation.get_complexity()

    def get_node_count(self) -> int:
        count = self.units
        for name in self.op_names:
            count += self.__getattribute__(name).count_nodes()
        return count

    def hybrid_forward(self, F, x):
        res_list = []
        for name in self.op_names:
            res_list.append(self.__getattribute__(name)(x))
        if not res_list:
            res_list = [F.identity(x)]
        res = tuple(res_list)
        y = F.concat(*res, dim=1)
        y = self.out(y)
        y = F.reshape(y, (1, 1, self.batch_size, *self.model_shape))
        return y
