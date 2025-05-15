# (c) https://github.com/MontiCore/monticore

import mxnet as mx
import logging
import os
import numpy as np
import time
import shutil
from mxnet import gluon, autograd, nd

import CNNCreator_defaultGAN_defaultGANConnector_predictor
import CNNDataLoader_defaultGAN_defaultGANConnector_predictor
import CNNCreator_defaultGAN_defaultGANConnector_predictor
import CNNGanTrainer_defaultGAN_defaultGANConnector_predictor
from gan.CNNCreator_defaultGAN_defaultGANDiscriminator import CNNCreator_defaultGAN_defaultGANDiscriminator



if __name__ == "__main__":

    logging.basicConfig(level=logging.DEBUG)
    logger = logging.getLogger()
    handler = logging.FileHandler("train.log", "w", encoding=None, delay="true")
    logger.addHandler(handler)
    logging.getLogger('matplotlib.font_manager').disabled = True
    logging.getLogger('matplotlib.colorbar').disabled = True
    logger.addHandler(handler)

    data_loader = CNNDataLoader_defaultGAN_defaultGANConnector_predictor.CNNDataLoader_defaultGAN_defaultGANConnector_predictor()
    gen_creator = CNNCreator_defaultGAN_defaultGANConnector_predictor.CNNCreator_defaultGAN_defaultGANConnector_predictor()
    dis_creator = CNNCreator_defaultGAN_defaultGANDiscriminator()

    defaultGAN_defaultGANConnector_predictor_trainer = CNNGanTrainer_defaultGAN_defaultGANConnector_predictor.CNNGanTrainer_defaultGAN_defaultGANConnector_predictor(
        data_loader,
        gen_creator,
        dis_creator,
    )

    defaultGAN_defaultGANConnector_predictor_trainer.train(
        batch_size=64,
        num_epoch=10,
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
