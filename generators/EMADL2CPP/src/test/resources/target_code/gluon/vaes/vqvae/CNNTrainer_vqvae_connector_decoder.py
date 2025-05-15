# (c) https://github.com/MontiCore/monticore

import mxnet as mx
import logging
import os
import numpy as np
import time
import shutil
from mxnet import gluon, autograd, nd

import CNNCreator_vqvae_connector_decoder
import CNNDataLoader_vqvae_connector_decoder
import CNNAutoencoderTrainer_vqvae_encoder
from CNNCreator_vqvae_encoder import CNNCreator_vqvae_encoder



if __name__ == "__main__":

    logging.basicConfig(level=logging.DEBUG)
    logger = logging.getLogger()
    handler = logging.FileHandler("train.log", "w", encoding=None, delay="true")
    logger.addHandler(handler)
    logging.getLogger('matplotlib.font_manager').disabled = True
    logging.getLogger('matplotlib.colorbar').disabled = True
    logger.addHandler(handler)

    data_loader = CNNDataLoader_vqvae_connector_decoder.CNNDataLoader_vqvae_connector_decoder()
    encoder_creator = CNNCreator_vqvae_encoder()
    decoder_creator = CNNCreator_vqvae_connector_decoder.CNNCreator_vqvae_connector_decoder()
    vqvae_connector_decoder_trainer = CNNAutoencoderTrainer_vqvae_encoder.CNNAutoencoderTrainer(
    data_loader,
    encoder_creator,
    decoder_creator,
    )

    vqvae_connector_decoder_trainer.train(
        batch_size=200,
        num_epoch=15,
        checkpoint_period=5,
        context='gpu',
        normalize=False,
        preprocessing=False,
        optimizer='adam',
        optimizer_params={
            'learning_rate': 1.0E-4        },
        reconstruction_loss='mse',
        kl_loss_weight=1,
        print_images=True,
)
