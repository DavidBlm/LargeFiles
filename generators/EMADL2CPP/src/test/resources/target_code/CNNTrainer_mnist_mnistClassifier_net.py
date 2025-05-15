# (c) https://github.com/MontiCore/monticore
from caffe2.python import workspace, core, model_helper, brew, optimizer
from caffe2.python.predictor import mobile_exporter
from caffe2.proto import caffe2_pb2

import numpy as np
import logging
import CNNCreator_mnist_mnistClassifier_net
import CNNDataCleaner_mnist_mnistClassifier_net

if __name__ == "__main__":
    logging.basicConfig(level=logging.DEBUG)
    logger = logging.getLogger()
    handler = logging.FileHandler("train.log", "w", encoding=None, delay="true")
    logger.addHandler(handler)

    mnist_mnistClassifier_net_cleaner = CNNDataCleaner_mnist_mnistClassifier_net.CNNDataCleaner_mnist_mnistClassifier_net()
    mnist_mnistClassifier_net = CNNCreator_mnist_mnistClassifier_net.CNNCreator_mnist_mnistClassifier_net(
        mnist_mnistClassifier_net_cleaner
    )
    mnist_mnistClassifier_net.train(
        num_epoch=11,
        batch_size=64,
        context='gpu',
        eval_metric='accuracy',
        opt_type='adam',
            epsilon=1.0E-8,
            weight_decay=0.001,
            beta1=0.9,
            beta2=0.999,
            policy='fixed',
            base_learning_rate=0.001
    )
