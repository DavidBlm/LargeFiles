# (c) https://github.com/MontiCore/monticore

import mxnet as mx
import logging
import os
import numpy as np
import time
import shutil
from mxnet import gluon, autograd, nd

import CNNCreator_infoGAN_infoGANConnector_predictor
import CNNDataLoader_infoGAN_infoGANConnector_predictor
import CNNCreator_infoGAN_infoGANConnector_predictor
import CNNGanTrainer_infoGAN_infoGANConnector_predictor
from gan.CNNCreator_infoGAN_infoGANDiscriminator import CNNCreator_infoGAN_infoGANDiscriminator
from gan.CNNCreator_infoGAN_infoGANQNetwork import CNNCreator_infoGAN_infoGANQNetwork



if __name__ == "__main__":

    logging.basicConfig(level=logging.DEBUG)
    logger = logging.getLogger()
    handler = logging.FileHandler("train.log", "w", encoding=None, delay="true")
    logger.addHandler(handler)
    logging.getLogger('matplotlib.font_manager').disabled = True
    logging.getLogger('matplotlib.colorbar').disabled = True
    logger.addHandler(handler)

    data_loader = CNNDataLoader_infoGAN_infoGANConnector_predictor.CNNDataLoader_infoGAN_infoGANConnector_predictor()
    gen_creator = CNNCreator_infoGAN_infoGANConnector_predictor.CNNCreator_infoGAN_infoGANConnector_predictor()
    dis_creator = CNNCreator_infoGAN_infoGANDiscriminator()
    qnet_creator = CNNCreator_infoGAN_infoGANQNetwork()

    infoGAN_infoGANConnector_predictor_trainer = CNNGanTrainer_infoGAN_infoGANConnector_predictor.CNNGanTrainer_infoGAN_infoGANConnector_predictor(
        data_loader,
        gen_creator,
        dis_creator,
        qnet_creator
    )

    infoGAN_infoGANConnector_predictor_trainer.train(
        batch_size=64,
        num_epoch=5,
        load_checkpoint=False,
        context='cpu',
        normalize=False,
        preprocessing=False,
        optimizer='adam',
        optimizer_params={
            'beta1': 0.5,
            'learning_rate': 2.0E-4        },
        discriminator_optimizer= 'adam',
        discriminator_optimizer_params= {
            'beta1': 0.5,
            'learning_rate': 2.0E-4},
        noise_distribution = 'gaussian',
        noise_distribution_params = {
            'mean_value': 0,
            'spread_value': 1
     },
        noise_input="noise",
        log_period=10,
        print_images=True,
)
