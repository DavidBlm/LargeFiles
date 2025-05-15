from mxnet.gluon.loss import Loss
from mxnet.ndarray import add, concatenate
import mxnet.ndarray as nd
from mxnet import gluon, autograd
import mxnet as mx
from typing import List, Union
from AdaNetConfig import AdaNetConfig

try:
    import AdamW
except ModuleNotFoundError:
    pass


def objective_function(model: mx.gluon.HybridBlock,
                       training_data_iterator: mx.io.NDArrayIter, loss: mx.gluon.loss.Loss,
                       gamma=AdaNetConfig.GAMMA.value) -> nd.array:
    """
    :param model: Union[SuperCandidateHull, ModelTemplate]
    :param training_data_iterator:
    :param loss:
    :param gamma:
    :return:
    """
    training_data_iterator.reset()
    err_list = []
    for batch_i, batch in enumerate(training_data_iterator):
        pred = model(batch.data[0])[0][0]
        label = batch.label[0]
        error = loss(pred, label)
        err_list.append(error)
    err = concatenate(err_list)
    c_complexities = model.get_candidate_complexity()
    c_complexities = c_complexities * gamma
    objective = err.mean() + c_complexities.mean()

    return objective[0][0]


def calculate_l1(params: dict) -> float:
    """
    calculate the L1 Norm on the weights of the passed model
    """
    parameter = params
    l1 = None
    for key in parameter:
        if 'weight' in key:
            if l1 is None:
                l1 = parameter[key].data().abs().sum()
            else:
                l1 = add(l1, parameter[key].data().abs().sum())
    return l1


class CandidateTrainingLoss(Loss):
    def __init__(self,
                 weight=None,
                 candidate=None,
                 loss=None,
                 batch_axis=0,
                 alpha=AdaNetConfig.ALPHA.value,
                 beta=AdaNetConfig.BETA.value,
                 gamma=AdaNetConfig.GAMMA.value,
                 **kwargs):
        """
        loss function which is used to train each candidate

        :param  loss, can be any (binary) loss function, to the result a regularization term is
        added which consists of  complexity of the candidate and the L1-Norm applied
        to the candidate weights
        """
        super(CandidateTrainingLoss, self).__init__(weight, batch_axis, **kwargs)

        self.a = alpha  # weight for the rademacher approximation
        self.b = beta  # fixed added value to the weighted rademacher approximation
        self.g = gamma  # weight of the combined complexity of L1 and rademacher approximation

        self.coreLoss = loss  # in template, the loss function is passed initialized!!!!
        self.model = candidate  # candidate to be trained

    # noinspection PyMethodOverriding
    def hybrid_forward(self, F, x, label, *args, **kwargs):
        l1 = calculate_l1(self.model.collect_params())

        # calculate regularization term reg
        # reg = (alpha*r)+beta , r = rademacher complexity approximation
        rade_term = F.add(F.multiply(self.a, self.model.approximate_rade()), self.b)
        reg_term = F.multiply(rade_term, l1)

        # save the regularization term, since it is needed in the calculation of the objective function
        self.model.update_complexity(reg_term)

        # calculate the actual loss and add the regularization term
        core_loss = self.coreLoss(x, label)
        ad = F.multiply(F.ones(core_loss.shape), reg_term * self.g)

        res = F.add(core_loss, ad)

        return res


class AdaLoss(Loss):
    """
    objective function of the whole model
    """

    def __init__(self, weight=None, model=None, loss=None, batch_axis=0, lamb=AdaNetConfig.LAMBDA.value,
                 gamma=AdaNetConfig.GAMMA.value, beta=AdaNetConfig.BETA.value, **kwargs):
        super(AdaLoss, self).__init__(weight, batch_axis, **kwargs)
        self.g = gamma
        self.coreLoss = loss
        self.model = model
        self.c_complexities = self.model.get_candidate_complexity()  # get candidate complexities
        self.lamb = lamb
        self.beta = beta

    def hybrid_forward(self, F, x, label, **kwargs):
        cl = self.coreLoss(x, label)
        l1 = calculate_l1(self.model.out.collect_params())
        reg_term = F.sum(((self.lamb * self.c_complexities) + self.beta) * l1)
        return F.add(cl, reg_term * self.g)


def fit_component(train_data_iterator: mx.io.NDArrayIter, trainer: mx.gluon.Trainer, epochs: int,
                  component: gluon.HybridBlock,
                  loss_class: gluon.loss, loss_params: dict, model_flag: bool) -> List[float]:
    """
    function trains a component of the generated model.
    expects a component, a trainer instance with corresponding parameters.

    """
    loss_list = []
    loss = loss_class(**loss_params)
    for epoch in range(epochs):
        train_data_iterator.reset()
        for batch_i, batch in enumerate(train_data_iterator):
            with autograd.record():
                data = batch.data[0]
                label = batch.label[0]
                if model_flag:
                    pred = component(data)[0][0]
                else:
                    pred = component(data)
                error = loss(pred, label)
            error.backward()
            trainer.step(data.shape[0], ignore_stale_grad=True)

        loss_avg = error.mean().asscalar()
        loss_list.append(loss_avg)

    return loss_list


def train_candidate(candidate, epochs: int, optimizer: str, optimizer_params: dict,
                    train_data_iterator: mx.io.NDArrayIter, loss: Loss) -> List[float]:
    candidate_trainer = get_trainer(optimizer, candidate.collect_params(), optimizer_params)
    return fit_component(train_data_iterator=train_data_iterator, trainer=candidate_trainer, epochs=epochs,
                         component=candidate,
                         loss_class=CandidateTrainingLoss, loss_params={'loss': loss, 'candidate': candidate},
                         model_flag=False)


def train_model(candidate, epochs: int, optimizer: str, optimizer_params: dict, train_data_iterator: mx.io.NDArrayIter,
                loss: Loss) -> List[float]:
    params = candidate.out.collect_params()
    model_trainer = get_trainer(optimizer=optimizer, parameters=params, optimizer_params=optimizer_params)
    return fit_component(train_data_iterator=train_data_iterator, trainer=model_trainer, epochs=epochs,
                         component=candidate,
                         loss_class=AdaLoss, loss_params={'loss': loss, 'model': candidate}, model_flag=True)


def get_trainer(optimizer: str, parameters: mx.gluon.ParameterDict, optimizer_params: dict) -> mx.gluon.Trainer:
    # gluon.Trainer doesnt take a ctx
    if optimizer == 'Adamw':
        trainer = mx.gluon.Trainer(parameters, AdamW.AdamW(**optimizer_params))
    else:
        trainer = mx.gluon.Trainer(parameters, optimizer, optimizer_params)
    return trainer
