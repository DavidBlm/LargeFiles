import torch
import torch.nn as nn
import logging


def translate_loss_name(loss):
    mapping = {
        "l2": nn.MSELoss(),
        "l1": nn.L1Loss(),
        "cross_entropy": nn.CrossEntropyLoss(),
        "logistic": nn.SoftMarginLoss(),
        "hinge": nn.MultiMarginLoss(),
        "squared_hinge": nn.MSELoss(),
        "kullback_leibler": nn.KLDivLoss(),
        "softmax_cross_entropy": nn.CrossEntropyLoss(),
        "huber_loss": nn.HuberLoss()
    }

    if loss in mapping.keys():
        fixed_loss = mapping[loss]
    else:
        logging.warning("The following loss is not supported by the pytorch generator:%s \n", loss)
    return fixed_loss


def translate_optim_name(model_param, optim_name, optim_learning_rate, optim_weight_decay,
                         sgd_momentum, adam_betas, adam_epsilon):
    mapping = {
        "sgd": torch.optim.SGD(model_param, lr=optim_learning_rate, momentum=sgd_momentum, weight_decay=optim_weight_decay),
        "adam": torch.optim.Adam(model_param, lr=optim_learning_rate, betas=adam_betas, eps=adam_epsilon,
                                 weight_decay=optim_weight_decay)

    }

    if optim_name in mapping.keys():
        fixed_optim = mapping[optim_name]
    else:
        logging.warning("The following optimizer is not supported by the pytorch generator:%s \n", optim_name)
    return fixed_optim
