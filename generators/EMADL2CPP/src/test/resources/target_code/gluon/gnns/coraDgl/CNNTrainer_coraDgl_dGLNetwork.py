# (c) https://github.com/MontiCore/monticore
import logging
import pathlib

import mxnet as mx

import CNNCreator_coraDgl_dGLNetwork
import CNNDataLoader_coraDgl_dGLNetwork
from CNNDatasets_coraDgl_dGLNetwork import RetrainingConf
import CNNDataCleaner_coraDgl_dGLNetwork
import CNNSupervisedTrainer_coraDgl_dGLNetwork

if __name__ == "__main__":
    logger = logging.getLogger()
    logging.basicConfig(level=logging.DEBUG)
    handler = logging.FileHandler("train.log", "w", encoding=None, delay="true")
    logger.addHandler(handler)

    coraDgl_dGLNetwork_creator = CNNCreator_coraDgl_dGLNetwork.CNNCreator_coraDgl_dGLNetwork()
    coraDgl_dGLNetwork_creator.validate_parameters()
    coraDgl_dGLNetwork_cleaner = CNNDataCleaner_coraDgl_dGLNetwork.CNNDataCleaner_coraDgl_dGLNetwork()
    coraDgl_dGLNetwork_loader = CNNDataLoader_coraDgl_dGLNetwork.CNNDataLoader_coraDgl_dGLNetwork(
        coraDgl_dGLNetwork_cleaner
    )

    prev_dataset = None
    retraining_conf = coraDgl_dGLNetwork_loader.load_retraining_conf()
    
    coraDgl_dGLNetwork_trainer = CNNSupervisedTrainer_coraDgl_dGLNetwork.CNNSupervisedTrainer_coraDgl_dGLNetwork(
        coraDgl_dGLNetwork_loader,
        coraDgl_dGLNetwork_creator
    )

    for dataset in retraining_conf.changes:
        coraDgl_dGLNetwork_creator.dataset = dataset
        if(dataset.retraining):
            if prev_dataset: 
                logger.info("Retrain dataset %s on top of dataset %s.", dataset.id, prev_dataset.id)
            else: 
                logger.info("Dataset %s needs to be trained. Hash was different during the last EMADL2CPP run.", dataset.id)

            optimizer = 'adam'
            optimizer_params = {
                'weight_decay': 0.0,
                            'learning_rate': 0.05,
                            'learning_rate_decay': 1.0,
                            'step_size': 1}



            coraDgl_dGLNetwork_trainer.train(
                dataset=dataset,
                test_dataset=retraining_conf.testing,
                batch_size=1,
                num_epoch=20,
                load_checkpoint=False,
                load_pretrained=bool(prev_dataset),
                load_pretrained_dataset=prev_dataset,
                context='cpu',
                preprocessing=False,
                normalize=False,
                eval_metric='accuracy_masked',
                eval_metric_params={
                },
                eval_train=False,
                loss='softmax_cross_entropy',
                    loss_params={
                        'sparse_label': True        },
            optimizer=optimizer,
            optimizer_params=optimizer_params,
            )
        else: 
            logger.info("Skipped training of dataset %s. Training is not necessary", dataset.id)

        prev_dataset = dataset

