import logging
import mxnet as mx

import CNNCreator_cNNCalculator_connector_predictor6
import CNNDataLoader_cNNCalculator_connector_predictor6
import CNNSupervisedTrainer_cNNCalculator_connector_predictor6

if __name__ == "__main__":
    logging.basicConfig(level=logging.DEBUG)
    logger = logging.getLogger()
    handler = logging.FileHandler("train.log", "w", encoding=None, delay="true")
    logger.addHandler(handler)

    cNNCalculator_connector_predictor6_creator = CNNCreator_cNNCalculator_connector_predictor6.CNNCreator_cNNCalculator_connector_predictor6()
    cNNCalculator_connector_predictor6_creator.validate_parameters()
    cNNCalculator_connector_predictor6_loader = CNNDataLoader_cNNCalculator_connector_predictor6.CNNDataLoader_cNNCalculator_connector_predictor6()
    cNNCalculator_connector_predictor6_trainer = CNNSupervisedTrainer_cNNCalculator_connector_predictor6.CNNSupervisedTrainer_cNNCalculator_connector_predictor6(
        cNNCalculator_connector_predictor6_loader,
        cNNCalculator_connector_predictor6_creator
    )

    cNNCalculator_connector_predictor6_trainer.train(
        batch_size=10,
        num_epoch=1,
        load_checkpoint=False,
        context='cpu',
        preprocessing=False,
        normalize=False,
        optimizer='sgd',
        optimizer_params={
            'weight_decay': 0.0,
            'learning_rate': 0.1,
            'learning_rate_decay': 0.85,
            'step_size': 1000}
    )
