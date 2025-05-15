"""
    this module exposes the fit function for the AdaNet algorithm which returns a newly designed model
"""
import CoreAdaNet
import AdaNetDefault as DefaultAda

import adaNetUtils as anUtils
import numpy as np
import mxnet as mx
from mxnet import gluon
from mxnet import ndarray as nd
from AdaNetConfig import AdaNetConfig
import AdaNetDefault


def fit(loss: gluon.loss.Loss,
        optimizer: str,
        epochs: int,
        optimizer_params: dict,
        train_iter: mx.io.NDArrayIter,
        data_class: CoreAdaNet.DataClass,
        batch_size=10,
        ctx=None,
        logging=None) -> gluon.HybridBlock:
    logging.info(
        f"AdaNet: starting with {epochs} epoch(s) per training with batch_size:{batch_size} and in max {AdaNetConfig.MAX_NUM_ROUNDS.value} rounds  ...")

    cg = DefaultAda.Builder(batch_size=batch_size, model_shape=data_class.model_shape, optimizer=optimizer,
                            optimizer_params=optimizer_params, loss=loss, build_operation=data_class.block,
                            in_block=data_class.inBlock, out_block=data_class.outBlock, ctx=ctx, epochs=epochs,
                            train_iterator=train_iter)
    model_template = AdaNetDefault.ModelTemplate
    model_operations = {}
    model_score = None
    model = model_template(operations=model_operations, batch_size=batch_size, model_shape=data_class.model_shape)

    if ctx is None:
        ctx = mx.gpu() if mx.context.num_gpus() else mx.cpu()

    model.initialize(ctx=ctx)

    for rnd in range(AdaNetConfig.MAX_NUM_ROUNDS.value):
        candidates = cg.get_candidates()
        model_data = {}
        for name, data in candidates.items():
            candidate, candidate_loss = data
            model_eval = {}
            model_name = name + '_model'

            # add the current candidate as operation
            candidate_op = model_operations.copy()
            candidate_op[name] = candidate

            # create new model
            candidate_model = model_template(operations=candidate_op, batch_size=batch_size,
                                             model_shape=data_class.model_shape, )

            candidate_model.out.initialize(ctx=ctx)

            candidate_model.hybridize()

            model_loss = anUtils.train_model(candidate_model, epochs, optimizer, optimizer_params, train_iter, loss)
            objective_score = anUtils.objective_function(model=candidate_model, training_data_iterator=train_iter,
                                                         loss=loss)

            model_eval['model'] = candidate_model
            model_eval['score'] = objective_score
            model_eval['operation'] = candidate
            model_data[model_name] = model_eval
            logging_msg = "candidate average loss:{:.5f} model average loss:{:.5f} objective score: {:.5f}".format(
                np.mean(candidate_loss), np.mean(model_loss), objective_score.asscalar())
            logging.info(logging_msg)

        min_name = None
        min_score = None
        for name in model_data:
            score = model_data[name]['score']
            if min_score is None:
                min_score = score
                min_name = name
            elif min_score > score:
                min_name = name
                min_score = score
        model, operation, score = model_data[min_name]['model'], model_data[min_name]['operation'], \
                                  model_data[min_name]['score']

        if model_score is None:
            model_score = score
            old_score = nd.array(model_score)
        else:
            # if the new score is better than the old one continue else return current model
            improvement_in_percent = (1 - (model_score / old_score).asscalar()) * 100
            if score <= model_score and ((improvement_in_percent >= .5) or improvement_in_percent == 0):
                old_score = nd.array(model_score)
                model_score = nd.array(score)
            else:
                logging.info("AdaNet: abort in Round {}/{}".format(rnd + 1, AdaNetConfig.MAX_NUM_ROUNDS.value))
                break

        model_operations[operation.name] = operation
        cg.update()
        round_msg = 'AdaNet:round: {}/{} finished,'.format(rnd + 1, AdaNetConfig.MAX_NUM_ROUNDS.value)
        improvement = (1 - (model_score / old_score).asscalar()) * 100
        score_msg = 'current model score:{:.5f} improvement {:.5f}% current model node count:{}'.format(
            model_score.asscalar(), improvement, model.get_node_count())
        logging.info(round_msg + score_msg)

    model = model_template(operations=model_operations, generation=False, batch_size=batch_size,
                           model_shape=data_class.model_shape)
    model.hybridize()
    model.initialize(ctx=ctx, force_reinit=True)
    logging.info(model.get_emadl_repr())
    return model
