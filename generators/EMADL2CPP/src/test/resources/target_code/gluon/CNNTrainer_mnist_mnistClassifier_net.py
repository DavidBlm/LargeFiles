# (c) https://github.com/MontiCore/monticore
import logging
import pathlib

import mxnet as mx

import CNNCreator_mnist_mnistClassifier_net
import CNNDataLoader_mnist_mnistClassifier_net
import CNNDataCleaner_mnist_mnistClassifier_net
import CNNSupervisedTrainer_mnist_mnistClassifier_net
from CNNDatasets_mnist_mnistClassifier_net import RetrainingConf
import CNNDataCleaner_mnist_mnistClassifier_net
import CNNSupervisedTrainer_mnist_mnistClassifier_net

if __name__ == "__main__":
    logger = logging.getLogger()
    logging.basicConfig(level=logging.DEBUG)
    handler = logging.FileHandler("train.log", "w", encoding=None, delay="true")
    logger.addHandler(handler)

    mnist_mnistClassifier_net_creator = CNNCreator_mnist_mnistClassifier_net.CNNCreator_mnist_mnistClassifier_net()
    mnist_mnistClassifier_net_creator.validate_parameters()
    mnist_mnistClassifier_net_cleaner = CNNDataCleaner_mnist_mnistClassifier_net.CNNDataCleaner_mnist_mnistClassifier_net()
    mnist_mnistClassifier_net_loader = CNNDataLoader_mnist_mnistClassifier_net.CNNDataLoader_mnist_mnistClassifier_net(
        mnist_mnistClassifier_net_cleaner
    )

    mnist_mnistClassifier_net_trainer = CNNSupervisedTrainer_mnist_mnistClassifier_net.CNNSupervisedTrainer_mnist_mnistClassifier_net(
        mnist_mnistClassifier_net_loader,
        mnist_mnistClassifier_net_creator
    )

    for dataset in retraining_conf.changes:
        mnist_mnistClassifier_net_creator.dataset = dataset
        if(dataset.retraining):
            if prev_dataset: 
                logger.info("Retrain dataset %s on top of dataset %s.", dataset.id, prev_dataset.id)
            else: 
                logger.info("Dataset %s needs to be trained. Hash was different during the last EMADL2CPP run.", dataset.id)

            optimizer = 'adam'
            optimizer_params = {
                'epsilon': 1.0E-8,
                            'weight_decay': 0.001,
                            'beta1': 0.9,
                            'beta2': 0.999,
                            'learning_rate_policy': 'fixed',
                            'learning_rate': 0.001}



            mnist_mnistClassifier_net_trainer.train(
                dataset=dataset,
                test_dataset=retraining_conf.testing,
                batch_size=64,
                num_epoch=11,
                load_pretrained=bool(prev_dataset),
                load_pretrained_dataset=prev_dataset,
                context='gpu',
                preprocessing=False,
                eval_metric='accuracy',
                eval_metric_params={
                },
            optimizer=optimizer,
            optimizer_params=optimizer_params,
            )
        else: 
            logger.info("Skipped training of dataset %s. Training is not necessary", dataset.id)

        prev_dataset = dataset

