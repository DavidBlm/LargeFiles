# (c) https://github.com/MontiCore/monticore
import logging
import mxnet as mx
import CNNCreator_cifar10_cifar10Classifier_net
import CNNDataCleaner_cifar10_cifar10Classifier_net

if __name__ == "__main__":
    logging.basicConfig(level=logging.DEBUG)
    logger = logging.getLogger()
    handler = logging.FileHandler("train.log", "w", encoding=None, delay="true")
    logger.addHandler(handler)

    cifar10_cifar10Classifier_net_cleaner = CNNDataCleaner_cifar10_cifar10Classifier_net.CNNDataCleaner_cifar10_cifar10Classifier_net()
    cifar10_cifar10Classifier_net = CNNCreator_cifar10_cifar10Classifier_net.CNNCreator_cifar10_cifar10Classifier_net(
        cifar10_cifar10Classifier_net_cleaner
    )
    cifar10_cifar10Classifier_net.train(
        batch_size=5,
        num_epoch=10,
        load_checkpoint=False,
        context='cpu',
        normalize=True,
        optimizer='adam',
        optimizer_params={
        'weight_decay': 1.0E-4,
        'learning_rate': 0.01,
        'learning_rate_decay': 0.8,
        'step_size': 1000}
    )
