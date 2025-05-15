# (c) https://github.com/MontiCore/monticore
import logging
import pathlib

import mxnet as mx

import CNNCreator_cNNCalculator_connector_predictor1
import CNNDataLoader_cNNCalculator_connector_predictor1
from CNNDatasets_cNNCalculator_connector_predictor1 import RetrainingConf
import CNNDataCleaner_cNNCalculator_connector_predictor1
import CNNSupervisedTrainer_cNNCalculator_connector_predictor1

if __name__ == "__main__":
    logger = logging.getLogger()
    logging.basicConfig(level=logging.DEBUG)
    handler = logging.FileHandler("train.log", "w", encoding=None, delay="true")
    logger.addHandler(handler)

    cNNCalculator_connector_predictor1_creator = CNNCreator_cNNCalculator_connector_predictor1.CNNCreator_cNNCalculator_connector_predictor1()
    cNNCalculator_connector_predictor1_creator.validate_parameters()
    cNNCalculator_connector_predictor1_cleaner = CNNDataCleaner_cNNCalculator_connector_predictor1.CNNDataCleaner_cNNCalculator_connector_predictor1()
    cNNCalculator_connector_predictor1_loader = CNNDataLoader_cNNCalculator_connector_predictor1.CNNDataLoader_cNNCalculator_connector_predictor1(
        cNNCalculator_connector_predictor1_cleaner
    )

    prev_dataset = None
    retraining_conf = cNNCalculator_connector_predictor1_loader.load_retraining_conf()
    
    cNNCalculator_connector_predictor1_trainer = CNNSupervisedTrainer_cNNCalculator_connector_predictor1.CNNSupervisedTrainer_cNNCalculator_connector_predictor1(
        cNNCalculator_connector_predictor1_loader,
        cNNCalculator_connector_predictor1_creator
    )

    for dataset in retraining_conf.changes:
        cNNCalculator_connector_predictor1_creator.dataset = dataset
        if(dataset.retraining):
            if prev_dataset: 
                logger.info("Retrain dataset %s on top of dataset %s.", dataset.id, prev_dataset.id)
            else: 
                logger.info("Dataset %s needs to be trained. Hash was different during the last EMADL2CPP run.", dataset.id)

            optimizer = 'sgd'
            optimizer_params = {
                'weight_decay': 0.0,
                            'learning_rate': 0.1,
                            'learning_rate_decay': 0.85,
                            'step_size': 1000}



            cNNCalculator_connector_predictor1_trainer.train(
                dataset=dataset,
                test_dataset=retraining_conf.testing,
                batch_size=10,
                num_epoch=1,
                load_checkpoint=False,
                load_pretrained=bool(prev_dataset),
                load_pretrained_dataset=prev_dataset,
                context='cpu',
                preprocessing=False,
                normalize=False,
            optimizer=optimizer,
            optimizer_params=optimizer_params,
            )
        else: 
            logger.info("Skipped training of dataset %s. Training is not necessary", dataset.id)

        prev_dataset = dataset

