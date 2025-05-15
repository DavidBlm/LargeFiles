# (c) https://github.com/MontiCore/monticore
import inspect
import json
import logging
import math
import os
import pathlib
import pickle
import shutil
import sys
import time
import typing as t

import mxnet as mx
import numpy as np
from mxnet import autograd, gluon, nd
import dgl
try:
    import AdamW
except:
    pass
from CNNDatasets_coraDgl_dGLNetwork import TrainingDataset

logger = logging.getLogger(__name__)

class CrossEntropyLoss(gluon.loss.Loss):
    def __init__(self, axis=-1, sparse_label=True, weight=None, batch_axis=0, **kwargs):
        super(CrossEntropyLoss, self).__init__(weight, batch_axis, **kwargs)
        self._axis = axis
        self._sparse_label = sparse_label

    def hybrid_forward(self, F, pred, label, sample_weight=None):
        pred = F.log(pred)
        if self._sparse_label:
            loss = -F.pick(pred, label, axis=self._axis, keepdims=True)
        else:
            label = gluon.loss._reshape_like(F, label, pred)
            loss = -F.sum(pred * label, axis=self._axis, keepdims=True)
        loss = gluon.loss._apply_weighting(F, loss, self._weight, sample_weight)
        return F.mean(loss, axis=self._batch_axis, exclude=True)

class LogCoshLoss(gluon.loss.Loss):
    def __init__(self, weight=None, batch_axis=0, **kwargs):
        super(LogCoshLoss, self).__init__(weight, batch_axis, **kwargs)

    def hybrid_forward(self, F, pred, label, sample_weight=None):
        loss = F.log(F.cosh(pred - label))
        loss = gluon.loss._apply_weighting(F, loss, self._weight, sample_weight)
        return F.mean(loss, axis=self._batch_axis, exclude=True)

class SoftmaxCrossEntropyLossIgnoreIndices(gluon.loss.Loss):
    def __init__(self, axis=-1, ignore_indices=[], sparse_label=True, from_logits=False, weight=None, batch_axis=0, **kwargs):
        super(SoftmaxCrossEntropyLossIgnoreIndices, self).__init__(weight, batch_axis, **kwargs)
        self._axis = axis
        self._ignore_indices = ignore_indices
        self._sparse_label = sparse_label
        self._from_logits = from_logits

    def hybrid_forward(self, F, pred, label, sample_weight=None):
        log_softmax = F.log_softmax
        pick = F.pick
        if not self._from_logits:
            pred = log_softmax(pred, self._axis)
        if self._sparse_label:
            loss = -pick(pred, label, axis=self._axis, keepdims=True)
        else:
            label = gluon.loss._reshape_like(F, label, pred)
            loss = -(pred * label).sum(axis=self._axis, keepdims=True)
        # ignore some indices for loss, e.g. <pad> tokens in NLP applications
        for i in self._ignore_indices:
            loss = F.broadcast_mul(loss, F.logical_not(F.broadcast_equal(F.argmax(pred, axis=1), F.ones_like(F.argmax(pred, axis=1))*i) * F.broadcast_equal(F.argmax(pred, axis=1), label)))
        return loss.mean(axis=self._batch_axis, exclude=True)

class DiceLoss(gluon.loss.Loss):
    def __init__(self, axis=-1, sparse_label=True, from_logits=False, weight=None,
                 batch_axis=0, **kwargs):
        super(DiceLoss, self).__init__(weight, batch_axis, **kwargs)
        self._axis = axis
        self._sparse_label = sparse_label
        self._from_logits = from_logits

    def dice_loss(self, F, pred, label):
        smooth = 1.
        pred_y = F.argmax(pred, axis = self._axis)
        intersection = pred_y * label
        score = (2 * F.mean(intersection, axis=self._batch_axis, exclude=True) + smooth) \
            / (F.mean(label, axis=self._batch_axis, exclude=True) + F.mean(pred_y, axis=self._batch_axis, exclude=True) + smooth)

        return - F.log(score)

    def hybrid_forward(self, F, pred, label, sample_weight=None):
        if not self._from_logits:
            pred = F.log_softmax(pred, self._axis)
        if self._sparse_label:
            loss = -F.pick(pred, label, axis=self._axis, keepdims=True)
        else:
            label = gluon.loss._reshape_like(F, label, pred)
            loss = -F.sum(pred*label, axis=self._axis, keepdims=True)
        loss = gluon.loss._apply_weighting(F, loss, self._weight, sample_weight)
        diceloss = self.dice_loss(F, pred, label)
        return F.mean(loss, axis=self._batch_axis, exclude=True) + diceloss

class SoftmaxCrossEntropyLossIgnoreLabel(gluon.loss.Loss):
    def __init__(self, axis=-1, from_logits=False, weight=None,
                 batch_axis=0, ignore_label=255, **kwargs):
        super(SoftmaxCrossEntropyLossIgnoreLabel, self).__init__(weight, batch_axis, **kwargs)
        self._axis = axis
        self._from_logits = from_logits
        self._ignore_label = ignore_label

    def hybrid_forward(self, F, output, label, sample_weight=None):
        if not self._from_logits:
            output = F.log_softmax(output, axis=self._axis)

        valid_label_map = (label != self._ignore_label)
        loss = -(F.pick(output, label, axis=self._axis, keepdims=True) * valid_label_map )

        loss = gluon.loss._apply_weighting(F, loss, self._weight, sample_weight)
        return F.sum(loss) / F.sum(valid_label_map)

class LocalAdaptationLoss(gluon.loss.Loss):
    def __init__(self, lamb, axis=-1, sparse_label=True, weight=None, batch_axis=0,  **kwargs):
        super(LocalAdaptationLoss, self).__init__(weight, batch_axis, **kwargs)
        self.lamb = lamb
        self._axis = axis
        self._sparse_label = sparse_label

    def hybrid_forward(self, F, pred, label, curr_weights, base_weights, sample_weight=None):
        pred = F.log(pred)
        if self._sparse_label:
            cross_entr_loss = -F.pick(pred, label, axis=self._axis, keepdims=True)
        else:
            label = gluon.loss._reshape_like(F, label, pred)
            cross_entr_loss = -F.sum(pred * label, axis=self._axis, keepdims=True)
        cross_entr_loss = F.mean(cross_entr_loss, axis=self._batch_axis, exclude=True)

        weight_diff_loss = 0
        for param_key in base_weights:
            weight_diff_loss = F.add(weight_diff_loss, F.norm(curr_weights[param_key] - base_weights[param_key]))

        #this check is neccessary, otherwise if weight_diff_loss is zero (first training iteration)
        #the trainer would update the networks weights to nan, this must have somthing to do how
        #mxnet internally calculates the derivatives / tracks the weights
        if weight_diff_loss > 0:
            loss = self.lamb * weight_diff_loss + cross_entr_loss
            loss = gluon.loss._apply_weighting(F, loss, self._weight, sample_weight)
        else:
            loss = gluon.loss._apply_weighting(F, cross_entr_loss, self._weight, sample_weight)

        return loss    

@mx.metric.register
class ACCURACY_IGNORE_LABEL(mx.metric.EvalMetric):
    """Ignores a label when computing accuracy.
    """
    def __init__(self, axis=1, metric_ignore_label=255, name='accuracy',
                 output_names=None, label_names=None):
        super(ACCURACY_IGNORE_LABEL, self).__init__(
            name, axis=axis,
            output_names=output_names, label_names=label_names)
        self.axis = axis
        self.ignore_label = metric_ignore_label

    def update(self, labels, preds):
        mx.metric.check_label_shapes(labels, preds)

        for label, pred_label in zip(labels, preds):
            if pred_label.shape != label.shape:
                pred_label = mx.nd.argmax(pred_label, axis=self.axis, keepdims=True)
            label = label.astype('int32')
            pred_label = pred_label.astype('int32').as_in_context(label.context)

            mx.metric.check_label_shapes(label, pred_label)

            correct = mx.nd.sum( (label == pred_label) * (label != self.ignore_label) ).asscalar()
            total = mx.nd.sum( (label != self.ignore_label) ).asscalar()

            self.sum_metric += correct
            self.num_inst += total


@mx.metric.register
class ACCURACY_MASKED(mx.metric.EvalMetric):
    def __init__(self, axis=1, name='accuracy_masked', output_names=None, label_names=None):
        super(ACCURACY_MASKED, self).__init__(name, axis=axis, output_names=output_names, label_names=label_names)
        self.axis = axis

    def update(self, labels, preds, mask):
        mx.metric.check_label_shapes(labels, preds)
        if preds.shape != labels.shape:
            preds = mx.nd.argmax(preds, axis=self.axis, keepdims=True)
        labels = labels.astype('int32')
        preds = preds.astype('int32').as_in_context(labels.context)
        mask = mask.astype('int32').as_in_context(labels.context)
        mx.metric.check_label_shapes(labels, preds)

        correct = ((preds == labels)*mask).sum().asscalar()
        total = mask.sum().asscalar()
        self.sum_metric += correct
        self.num_inst += total


@mx.metric.register
class BLEU(mx.metric.EvalMetric):
    N = 4

    def __init__(self, exclude=None, name='bleu', output_names=None, label_names=None):
        super(BLEU, self).__init__(name=name, output_names=output_names, label_names=label_names)

        self._exclude = exclude or []

        self._match_counts = [0 for _ in range(self.N)]
        self._counts = [0 for _ in range(self.N)]

        self._size_ref = 0
        self._size_hyp = 0

    def update(self, labels, preds):
        labels, preds = mx.metric.check_label_shapes(labels, preds, True)

        new_labels = self._convert(labels)
        new_preds = self._convert(preds)

        for label, pred in zip(new_labels, new_preds):
            reference = [word for word in label if word not in self._exclude]
            hypothesis = [word for word in pred if word not in self._exclude]

            self._size_ref += len(reference)
            self._size_hyp += len(hypothesis)

            for n in range(self.N):
                reference_ngrams = self._get_ngrams(reference, n + 1)
                hypothesis_ngrams = self._get_ngrams(hypothesis, n + 1)

                match_count = 0

                for ngram in hypothesis_ngrams:
                    if ngram in reference_ngrams:
                        reference_ngrams.remove(ngram)

                        match_count += 1

                self._match_counts[n] += match_count
                self._counts[n] += len(hypothesis_ngrams)

    def get(self):
        precisions = [sys.float_info.min for n in range(self.N)]

        i = 1

        for n in range(self.N):
            match_counts = self._match_counts[n]
            counts = self._counts[n]

            if counts != 0:
                if match_counts == 0:
                    i *= 2
                    match_counts = 1 / i

                if (match_counts / counts) > 0:
                    precisions[n] = match_counts / counts

        bleu = self._get_brevity_penalty() * math.exp(sum(map(math.log, precisions)) / self.N)

        return (self.name, bleu)

    def calculate(self):
        precisions = [sys.float_info.min for n in range(self.N)]

        i = 1

        for n in range(self.N):
            match_counts = self._match_counts[n]
            counts = self._counts[n]

            if counts != 0:
                if match_counts == 0:
                    i *= 2
                    match_counts = 1 / i

                precisions[n] = match_counts / counts

        return self._get_brevity_penalty() * math.exp(sum(map(math.log, precisions)) / self.N)

    def _get_brevity_penalty(self):
        if self._size_hyp >= self._size_ref:
            return 1
        else:
            if self._size_hyp > 0:
                size_hyp = self._size_hyp
            else:
                size_hyp = 1

            return math.exp(1 - (self._size_ref / size_hyp))

    @staticmethod
    def _get_ngrams(sentence, n):
        ngrams = []

        if len(sentence) >= n:
            for i in range(len(sentence) - n + 1):
                ngrams.append(sentence[i:i+n])

        return ngrams

    @staticmethod
    def _convert(nd_list):
        if len(nd_list) == 0:
            return []

        new_list = [[] for _ in range(nd_list[0].shape[0])]

        for element in nd_list:
            for i in range(element.shape[0]):
                new_list[i].append(element[i].asscalar())

        return new_list



class CNNSupervisedTrainer_coraDgl_dGLNetwork:
    def __init__(self, data_loader, net_constructor):
        self._data_loader = data_loader
        self._net_creator = net_constructor
        self._networks = {}

    def train(self, batch_size=64,
              num_epoch=10,
              eval_metric='acc',
              eval_metric_params={},
              eval_train=False,
              loss ='softmax_cross_entropy',
              loss_params={},
              optimizer='adam',
              optimizer_params=(('learning_rate', 0.001),),
              load_checkpoint=True,
              checkpoint_period=5,
              dataset=None,
              test_dataset=None,
              load_pretrained=False,
              load_pretrained_dataset=None,
              log_period=50,
              context='gpu',
              save_attention_image=False,
              use_teacher_forcing=False,
              normalize=True,
              shuffle_data=False,
              clip_global_grad_norm=None,
              preprocessing=False,
              train_mask=None,
              test_mask=None,
              multi_graph=False,
              onnx_export=False,
              retraining_type: str = "automatically"
    ):
        logging.getLogger().setLevel(logging.DEBUG)
        num_pus = 1
        if context == 'gpu':
            num_pus = mx.context.num_gpus()
            if num_pus >= 1:
                if num_pus == 1:
                    mx_context = [mx.gpu(0)]
                else:
                    mx_context = [mx.gpu(i) for i in range(num_pus)]
            else:
                logging.error("Context argument is '" + context + "'. But no gpu is present in the system.")
        elif context == 'cpu':
            mx_context = [mx.cpu()]
        else:
            logging.error("Context argument is '" + context + "'. Only 'cpu' and 'gpu are valid arguments'.")
        single_pu_batch_size = int(batch_size/num_pus)

        if preprocessing:
            preproc_lib = "CNNPreprocessor_coraDgl_dGLNetwork_executor"
            train_iter, test_iter, data_mean, data_std, train_images, test_images = self._data_loader.load_preprocessed_data(batch_size, preproc_lib, shuffle_data)
        else:
            train_iter, test_iter, data_mean, data_std, train_images, test_images, train_graph, test_graph = self._data_loader.load_data(batch_size, shuffle_data, multi_graph, dataset, test_dataset)

        if 'weight_decay' in optimizer_params:
            optimizer_params['wd'] = optimizer_params['weight_decay']
            del optimizer_params['weight_decay']
        if 'learning_rate_decay' in optimizer_params:
            min_learning_rate = 1e-08
            if 'learning_rate_minimum' in optimizer_params:
                min_learning_rate = optimizer_params['learning_rate_minimum']
                del optimizer_params['learning_rate_minimum']
            optimizer_params['lr_scheduler'] = mx.lr_scheduler.FactorScheduler(
                                                   optimizer_params['step_size'],
                                                   factor=optimizer_params['learning_rate_decay'],
                                                   stop_factor_lr=min_learning_rate)
            del optimizer_params['step_size']
            del optimizer_params['learning_rate_decay']



        if normalize:
            self._net_creator.construct(context=mx_context, batch_size=batch_size, data_mean=data_mean, data_std=data_std)
        else:
            self._net_creator.construct(context=mx_context, batch_size=batch_size)

        begin_epoch = 0
        if load_checkpoint:
            begin_epoch = self._net_creator.load(mx_context)
        elif load_pretrained:
            self._net_creator.load_pretrained_weights(mx_context, load_pretrained_dataset)
        else:
            model_dir = self.parameter_path(0, dataset).parent
            if model_dir.exists():
                shutil.rmtree(model_dir)

        self._networks = self._net_creator.networks

        network_optimizer_params = {i: optimizer_params for i, _ in self._networks.items()}

        if load_pretrained and retraining_type == "automatically":
            for i, _ in self._networks.items():
                network_optimizer_params[i]["learning_rate"] = self.load_learning_rate(load_pretrained_dataset, network_optimizer_params[i], i)

        try:
            os.makedirs(self._net_creator._model_basedir_)
        except OSError:
            if not os.path.isdir(self._net_creator._model_basedir_):
                raise

        if optimizer == "adamw":
            trainers = [mx.gluon.Trainer(network.collect_params(), AdamW.AdamW(**network_optimizer_params[i])) for i, network in self._networks.items() if len(network.collect_params().values()) != 0]
        else:
            trainers = [mx.gluon.Trainer(network.collect_params(), optimizer, network_optimizer_params[i]) for i, network in self._networks.items() if len(network.collect_params().values()) != 0]

        margin = loss_params['margin'] if 'margin' in loss_params else 1.0
        sparseLabel = loss_params['sparse_label'] if 'sparse_label' in loss_params else True
        ignore_indices = [loss_params['ignore_indices']] if 'ignore_indices' in loss_params else []
        loss_axis = loss_params['loss_axis'] if 'loss_axis' in loss_params else -1
        batch_axis = loss_params['batch_axis'] if 'batch_axis' in loss_params else 0
        if loss == 'softmax_cross_entropy':
            fromLogits = loss_params['from_logits'] if 'from_logits' in loss_params else False
            loss_function = mx.gluon.loss.SoftmaxCrossEntropyLoss(axis=loss_axis, from_logits=fromLogits, sparse_label=sparseLabel, batch_axis=batch_axis)
        elif loss == 'softmax_cross_entropy_ignore_indices':
            fromLogits = loss_params['from_logits'] if 'from_logits' in loss_params else False
            loss_function = SoftmaxCrossEntropyLossIgnoreIndices(axis=loss_axis, ignore_indices=ignore_indices, from_logits=fromLogits, sparse_label=sparseLabel, batch_axis=batch_axis)
        elif loss == 'sigmoid_binary_cross_entropy':
            loss_function = mx.gluon.loss.SigmoidBinaryCrossEntropyLoss()
        elif loss == 'cross_entropy':
            loss_function = CrossEntropyLoss(axis=loss_axis, sparse_label=sparseLabel, batch_axis=batch_axis)
        elif loss == 'dice_loss':
            loss_weight = loss_params['loss_weight'] if 'loss_weight' in loss_params else None
            loss_function = DiceLoss(axis=loss_axis, weight=loss_weight, sparse_label=sparseLabel, batch_axis=batch_axis)
        elif loss == 'softmax_cross_entropy_ignore_label':
            loss_weight = loss_params['loss_weight'] if 'loss_weight' in loss_params else None
            loss_ignore_label = loss_params['loss_ignore_label'] if 'loss_ignore_label' in loss_params else None
            loss_function = SoftmaxCrossEntropyLossIgnoreLabel(axis=loss_axis, ignore_label=loss_ignore_label, weight=loss_weight, batch_axis=batch_axis)
        elif loss == 'l2':
            loss_function = mx.gluon.loss.L2Loss()
        elif loss == 'l1':
            loss_function = mx.gluon.loss.L1Loss()
        elif loss == 'huber':
            rho = loss_params['rho'] if 'rho' in loss_params else 1
            loss_function = mx.gluon.loss.HuberLoss(rho=rho)
        elif loss == 'hinge':
            loss_function = mx.gluon.loss.HingeLoss(margin=margin)
        elif loss == 'squared_hinge':
            loss_function = mx.gluon.loss.SquaredHingeLoss(margin=margin)
        elif loss == 'logistic':
            labelFormat = loss_params['label_format'] if 'label_format' in loss_params else 'signed'
            loss_function = mx.gluon.loss.LogisticLoss(label_format=labelFormat)
        elif loss == 'kullback_leibler':
            fromLogits = loss_params['from_logits'] if 'from_logits' in loss_params else True
            loss_function = mx.gluon.loss.KLDivLoss(from_logits=fromLogits)
        elif loss == 'log_cosh':
            loss_function = LogCoshLoss()
        else:
            logging.error("Invalid loss parameter.")

        loss_function.hybridize()


        tic = None

        avg_speed = 0
        n = 0
    
        for epoch in range(begin_epoch, begin_epoch + num_epoch):
            if shuffle_data:
                if preprocessing:
                    preproc_lib = "CNNPreprocessor_coraDgl_dGLNetwork_executor"
                    train_iter, test_iter, data_mean, data_std, train_images, test_images = self._data_loader.load_preprocessed_data(batch_size, preproc_lib, shuffle_data)
                else:
                    train_iter, test_iter, data_mean, data_std, train_images, test_images, train_graph, test_graph = self._data_loader.load_data(batch_size, shuffle_data, multi_graph, dataset, test_dataset)

            global_loss_train = 0.0
            train_batches = 0

            loss_total = 0
            train_iter.reset()
            for batch_i, batch in enumerate(train_iter):
                
                                 
                with autograd.record():
                    labels = [gluon.utils.split_and_load(batch.label[i], ctx_list=mx_context, even_split=False) for i in range(1)]
                    train_data_index = 0
                    graph_ = [dgl.batch(train_graph[batch_size*train_batches:min(len(train_graph), batch_size*(train_batches+1))])]
                    if 'features_' in graph_[0].ndata:
                        features_ = [graph_[0].ndata['features_']]
                    elif 'features_' in graph_[0].edata:
                        features_ = [graph_[0].edata['features_']]
                    else:
                        features_ = gluon.utils.split_and_load(batch.data[train_data_index], ctx_list=mx_context, even_split=False)
                        train_data_index += 1
                    if not multi_graph:
                        features_ = features_[0]

                    predictions_ = [mx.nd.zeros((single_pu_batch_size, 2708, 7,), ctx=context) for context in mx_context]
                    layerOne_output_ = [mx.nd.zeros((single_pu_batch_size, 1, 2708, 16,), ctx=context) for context in mx_context]


                    nd.waitall()
                    lossList = []
                    for i in range(num_pus):
                        lossList.append([])

                    net_ret = [self._networks[0](graph_[i], features_[i]) for i in range(num_pus)]
                    layerOne_output_ = [net_ret[i][0][0] for i in range(num_pus)]

                    net_ret = [self._networks[1](graph_[i], layerOne_output_[i]) for i in range(num_pus)]
                    predictions_ = [net_ret[i][0][0] for i in range(num_pus)]
                    if (train_mask is not None or test_mask is not None) and epoch == 0:
                        train_mask = self.get_mask_array(predictions_[0].shape[0], train_mask)
                        test_mask = self.get_mask_array(predictions_[0].shape[0], test_mask)
                    elif not multi_graph and epoch == 0:
                        train_mask = graph_[0].ndata['train_mask']
                        test_mask = graph_[0].ndata['test_mask']
                    if train_mask is not None:
                        outputs = [predictions_[0]]
                        [lossList[i].append(loss_function(mx.nd.squeeze(predictions_[i]), mx.nd.squeeze(labels[0][i]), mx.nd.expand_dims(train_mask, 1)).sum() / train_mask.sum().asscalar()) for i in range(num_pus)]
                    else:
                        [lossList[i].append(loss_function(predictions_[i], labels[0][i])) for i in range(num_pus)]



                    losses = [0]*num_pus
                    for i in range(num_pus):
                        for element in lossList[i]:
                            losses[i] = losses[i] + element

                for loss in losses: 
                    loss.backward()
                    loss_total += loss.sum().asscalar()
                    global_loss_train += loss.sum().asscalar()

                train_batches += 1

                if clip_global_grad_norm:
                    grads = []

                    for network in self._networks.values():
                        grads.extend([param.grad(mx_context) for param in network.collect_params().values()])

                    gluon.utils.clip_global_norm(grads, clip_global_grad_norm)

                for trainer in trainers:
                    trainer.step(batch_size)
    
                if tic is None:
                    tic = time.time()
                else:
                    if batch_i % log_period == 0:
                        try:
                            speed = log_period * batch_size / (time.time() - tic)
                        except ZeroDivisionError:
                            speed = float("inf")

                        loss_avg = loss_total / (batch_size * log_period)
                        loss_total = 0

                        logging.info("Epoch[%d] Batch[%d] Speed: %.2f samples/sec Loss: %.5f" % (epoch, batch_i, speed, loss_avg))
                        
                        avg_speed += speed
                        n += 1
    
                        tic = time.time()

            global_loss_train /= (train_batches * batch_size)

            tic = None
    
    
            if eval_train:
                train_iter.batch_size = single_pu_batch_size
                train_iter.reset()
                metric = mx.metric.create(eval_metric, **eval_metric_params)
                for batch_i, batch in enumerate(train_iter):
                    if True: # This statement is needed because of different indentations in FreeMarker

                        labels = [batch.label[i].as_in_context(mx_context[0]) for i in range(1)]
                        test_input_index = 0
                        graph_ = dgl.batch(test_graph[batch_size*test_batches:min(len(test_graph), batch_size*(test_batches+1))])
                        if 'features_' in graph_.ndata:
                            features_ = graph_.ndata['features_']
                        elif 'features_' in graph_.edata:
                            features_ = graph_.edata['features_']
                        else:
                            features_ = batch.data[test_input_index].as_in_context(mx_context[0])
                            test_input_index += 1

                        predictions_ = mx.nd.zeros((single_pu_batch_size, 2708, 7,), ctx=mx_context[0])
                        layerOne_output_ = mx.nd.zeros((single_pu_batch_size, 1, 2708, 16,), ctx=mx_context[0])


                        nd.waitall()

                        lossList = []
                        outputs = []
                        attentionList = []

                        net_ret = self._networks[0](graph_, features_)
                        layerOne_output_ = net_ret[0][0]
                        net_ret = self._networks[1](graph_, layerOne_output_)
                        predictions_ = net_ret[0][0]
                        outputs.append(predictions_)
                        lossList.append(loss_function(predictions_, labels[0]))
    
                    if save_attention_image == "True":
                        import matplotlib
                        matplotlib.use('Agg')
                        import matplotlib.pyplot as plt
                        logging.getLogger('matplotlib').setLevel(logging.ERROR)

                        if(os.path.isfile('src/test/resources/training_data/Show_attend_tell/dict.pkl')):
                            with open('src/test/resources/training_data/Show_attend_tell/dict.pkl', 'rb') as f:
                                dict = pickle.load(f)

                        plt.clf()
                        fig = plt.figure(figsize=(15,15))
                        max_length = len(labels)-1

                        ax = fig.add_subplot(max_length//3, max_length//4, 1)
                        ax.imshow(train_images[0+single_pu_batch_size*(batch_i)].transpose(1,2,0))

                        for l in range(max_length):
                            attention = attentionList[l]
                            attention = mx.nd.slice_axis(attention, axis=0, begin=0, end=1).squeeze()
                            attention_resized = np.resize(attention.asnumpy(), (8, 8))
                            ax = fig.add_subplot(max_length//3, max_length//4, l+2)
                            if int(labels[l+1][0].asscalar()) > len(dict):
                                ax.set_title("<unk>")
                            elif dict[int(labels[l+1][0].asscalar())] == "<end>":
                                ax.set_title(".")
                                img = ax.imshow(train_images[0+single_pu_batch_size*(batch_i)].transpose(1,2,0))
                                ax.imshow(attention_resized, cmap='gray', alpha=0.6, extent=img.get_extent())
                                break
                            else:
                                ax.set_title(dict[int(labels[l+1][0].asscalar())])
                            img = ax.imshow(train_images[0+single_pu_batch_size*(batch_i)].transpose(1,2,0))
                            ax.imshow(attention_resized, cmap='gray', alpha=0.6, extent=img.get_extent())

                        plt.tight_layout()
                        target_dir = 'target/attention_images'
                        if not os.path.exists(target_dir):
                            os.makedirs(target_dir)
                        plt.savefig(target_dir + '/attention_train.png')
                        plt.close()

                    predictions = []
                    for output_name in outputs:
                        if mx.nd.shape_array(mx.nd.squeeze(output_name)).size > 1:
                            predictions.append(mx.nd.argmax(output_name, axis=1))
                        else:
                            predictions.append(output_name)

                    metric.update(preds=predictions, labels=[labels[j] for j in range(len(labels))])

                train_metric_score = metric.get()[1]
            else:
                train_metric_score = 0

            global_loss_test = 0.0
            test_batches = 0
            test_metric_score = 0.0

            if test_iter: 
                test_iter.batch_size = single_pu_batch_size
                test_iter.reset()
                metric = mx.metric.create(eval_metric, **eval_metric_params)
                for batch_i, batch in enumerate(test_iter):
                    if test_mask is None:

                        labels = [batch.label[i].as_in_context(mx_context[0]) for i in range(1)]
                        test_input_index = 0
                        graph_ = dgl.batch(test_graph[batch_size*test_batches:min(len(test_graph), batch_size*(test_batches+1))])
                        if 'features_' in graph_.ndata:
                            features_ = graph_.ndata['features_']
                        elif 'features_' in graph_.edata:
                            features_ = graph_.edata['features_']
                        else:
                            features_ = batch.data[test_input_index].as_in_context(mx_context[0])
                            test_input_index += 1

                        predictions_ = mx.nd.zeros((single_pu_batch_size, 2708, 7,), ctx=mx_context[0])
                        layerOne_output_ = mx.nd.zeros((single_pu_batch_size, 1, 2708, 16,), ctx=mx_context[0])


                        nd.waitall()

                        lossList = []
                        outputs = []
                        attentionList = []

                        net_ret = self._networks[0](graph_, features_)
                        layerOne_output_ = net_ret[0][0]
                        net_ret = self._networks[1](graph_, layerOne_output_)
                        predictions_ = net_ret[0][0]
                        outputs.append(predictions_)
                        lossList.append(loss_function(predictions_, labels[0]))

                        if save_attention_image == "True":
                            if not eval_train:
                                import matplotlib
                                matplotlib.use('Agg')
                                import matplotlib.pyplot as plt
                                logging.getLogger('matplotlib').setLevel(logging.ERROR)

                                if(os.path.isfile('src/test/resources/training_data/Show_attend_tell/dict.pkl')):
                                    with open('src/test/resources/training_data/Show_attend_tell/dict.pkl', 'rb') as f:
                                        dict = pickle.load(f)

                            plt.clf()
                            fig = plt.figure(figsize=(15,15))
                            max_length = len(labels)-1

                            ax = fig.add_subplot(max_length//3, max_length//4, 1)
                            ax.imshow(test_images[0+single_pu_batch_size*(batch_i)].transpose(1,2,0))

                            for l in range(max_length):
                                attention = attentionList[l]
                                attention = mx.nd.slice_axis(attention, axis=0, begin=0, end=1).squeeze()
                                attention_resized = np.resize(attention.asnumpy(), (8, 8))
                                ax = fig.add_subplot(max_length//3, max_length//4, l+2)
                                if int(mx.nd.slice_axis(outputs[l+1], axis=0, begin=0, end=1).squeeze().asscalar()) > len(dict):
                                    ax.set_title("<unk>")
                                elif dict[int(mx.nd.slice_axis(outputs[l+1], axis=0, begin=0, end=1).squeeze().asscalar())] == "<end>":
                                    ax.set_title(".")
                                    img = ax.imshow(test_images[0+single_pu_batch_size*(batch_i)].transpose(1,2,0))
                                    ax.imshow(attention_resized, cmap='gray', alpha=0.6, extent=img.get_extent())
                                    break
                                else:
                                    ax.set_title(dict[int(mx.nd.slice_axis(outputs[l+1], axis=0, begin=0, end=1).squeeze().asscalar())])
                                img = ax.imshow(test_images[0+single_pu_batch_size*(batch_i)].transpose(1,2,0))
                                ax.imshow(attention_resized, cmap='gray', alpha=0.6, extent=img.get_extent())

                            plt.tight_layout()
                            target_dir = 'target/attention_images'
                            if not os.path.exists(target_dir):
                                os.makedirs(target_dir)
                            plt.savefig(target_dir + '/attention_test.png')
                            plt.close()

                    loss = 0
                    if test_mask is None:
                        for element in lossList:
                            loss = loss + element
                        global_loss_test += loss.sum().asscalar()

                    test_batches += 1

                    predictions = []
                    for output_name in outputs:
                        if mx.nd.shape_array(mx.nd.squeeze(output_name)).size > 1:
                            predictions.append(mx.nd.argmax(output_name, axis=1))
                        else:
                            predictions.append(output_name)
                    if test_mask is not None:
                        metric.update(preds=predictions[0], labels=mx.nd.squeeze(labels[0][0]), mask=test_mask)
                    else:
                        metric.update(preds=predictions, labels=[labels[j] for j in range(len(labels))])
                global_loss_test /= (test_batches * single_pu_batch_size)
                test_metric_name = metric.get()[0]
                test_metric_score = metric.get()[1]

                metric_file = open(self.parameter_path(i, dataset) / 'metric.txt', 'w')
                metric_file.write(test_metric_name + " " + str(test_metric_score))
                metric_file.close()

                metric_json_file = self.parameter_path(i, dataset) / 'metric.json'
                if not metric_json_file.exists():
                    metric_json_file.touch()
                try:
                    data = json.loads(metric_json_file.read_text())
                except json.decoder.JSONDecodeError:
                    data = []

                data.append(test_metric_score)
                metric_json_file.write_text(json.dumps(data))

            logging.info("Epoch[%d] Train metric: %f, Test metric: %f, Train loss: %f, Test loss: %f" % (epoch, train_metric_score, test_metric_score, global_loss_train, global_loss_test))

            if (epoch+1) % checkpoint_period == 0:
                for i, network in self._networks.items():
                    param_path = str(self.parameter_path(i, dataset) / (str(epoch).zfill(4) + '.params'))
                    network.save_parameters(param_path)
                    logger.info("Saved parameters to %s", param_path)


        for i, network in self._networks.items():
            param_path = str(self.parameter_path(i, dataset) / (str((num_epoch-1) + begin_epoch).zfill(4) + '.params'))
            network.save_parameters(param_path)
            logger.info("Saved parameters to %s", param_path)
            if onnx_export:
                from mxnet.contrib import onnx as onnx_mxnet
                input_shapes = [(1,) + d.shape[1:] for _, d in test_iter.data]
                model_path = self.parameter_path(i, dataset) / 'newest'
                onnx_mxnet.export_model(str(model_path) + '-symbol.json'), str(model_path) + '-0000.params', input_shapes, np.float32, str(model_path) + '.onnx'

            try:
                loss_function.export(str(self.parameter_path(i, dataset) / 'newest_loss'), epoch=0)
            except RuntimeError:
                logging.info("Forward for loss functions was not run, export is not possible.")

            # Save learning rate
            try:
                learning_rate_path = self.parameter_path(i, dataset) / 'learning_rate.txt'
                learning_rate_path.write_text(str(trainers[i].learning_rate))
                logger.info("Saved learning rate to %s", str(learning_rate_path))
            except IndexError:
                logging.warning("Failure during saving the learning rate.")

    def get_mask_array(self, shape, mask):
        if mask is None:
            return None
        idx = range(mask[0], mask[1])
        mask_array = np.zeros(shape)
        mask_array[idx] = 1
        mask_array = mx.nd.array(mask_array)
        return mask_array


    def parameter_path(self, index, dataset) -> pathlib.Path:
        path: pathlib.Path = self._net_creator.get_model_dir(index, dataset)
        path.mkdir(parents=True, exist_ok=True)
        return path

    def load_learning_rate(self, dataset: TrainingDataset, optimizer_params, index: int) -> t.Optional[float]:
        """Load learning rate from file"""

        try:
            learning_rate = float(pathlib.Path(self.parameter_path(index, dataset) / "learning_rate.txt").read_text())
            logging.info("Loaded learning rate %.4f for dataset %s and network %s", learning_rate, dataset.id, index)
            return learning_rate
        except FileNotFoundError:
            logging.warning("No learning rate found for dataset %s. Fallback to optimizer learning rate.", dataset.id)
            return optimizer_params.get("learning_rate", None)

        return None
