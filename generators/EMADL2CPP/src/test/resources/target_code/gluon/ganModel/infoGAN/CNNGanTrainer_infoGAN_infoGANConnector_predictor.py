# (c) https://github.com/MontiCore/monticore
import mxnet as mx
import logging
import numpy as np
import time
import os
import shutil
from mxnet import gluon, autograd, nd

class CrossEntropyLoss(gluon.loss.Loss):
    def __init__(self, axis=-1, sparse_label=True, weight=None, batch_axis=0, **kwargs):
        super(CrossEntropyLoss, self).__init__(weight, batch_axis, **kwargs)
        self._axis = axis
        self._sparse_label = sparse_label

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
            label = _reshape_like(F, label, pred)
            loss = -(pred * label).sum(axis=self._axis, keepdims=True)
        # ignore some indices for loss, e.g. <pad> tokens in NLP applications
        for i in self._ignore_indices:
            loss = loss * mx.nd.logical_not(mx.nd.equal(mx.nd.argmax(pred, axis=1), mx.nd.ones_like(mx.nd.argmax(pred, axis=1))*i) * mx.nd.equal(mx.nd.argmax(pred, axis=1), label))
        return loss.mean(axis=self._batch_axis, exclude=True)

from matplotlib import pyplot

def visualize(img_arr):
    img_np = img_arr.asnumpy().transpose(1, 2, 0)
    img_np = ((img_np+1) * 127.5).astype(np.uint8)

    if not img_np.shape[2] == 1:
        pyplot.axis('off')
        pyplot.imshow(img_np)
    else:
        pyplot.axis('off')
        s = img_np.shape
        img_np = img_np.reshape((s[0], s[1]))
        pyplot.imshow(img_np, cmap = 'Greys')

class CNNGanTrainer_infoGAN_infoGANConnector_predictor:

    def __init__(self, data_loader, net_constructor_gen, net_constructor_dis, net_constructor_qnet = None):
        self._data_loader = data_loader
        self._net_creator_gen = net_constructor_gen
        self._net_creator_dis = net_constructor_dis

        if net_constructor_qnet == None:
            self.use_qnet = False
        else:
            self._net_creator_qnet = net_constructor_qnet
            self.use_qnet = True

    def train(self, batch_size=64,
              num_epoch=10,
              eval_metric='acc',
              optimizer='adam',
              optimizer_params=(('learning_rate', 0.001),),
              load_checkpoint=True,
              context='gpu',
              checkpoint_period=5,
              normalize=True,
              noise_distribution='gaussian',
              noise_distribution_params=(('mean_value', 0),('spread_value', 1),),
              discriminator_optimizer='adam',
              discriminator_optimizer_params=(('learning_rate', 0.001),),
              constraint_distributions={},
              constraint_losses={},
              preprocessing = False,
              k_value = 1,
              generator_loss = None,
              generator_target_name = "",
              noise_input = "",
              gen_loss_weight = 1,
              dis_loss_weight = 1,
              log_period = 50,
              print_images = False):

        if context == 'gpu':
            mx_context = mx.gpu()
        elif context == 'cpu':
            mx_context = mx.cpu()
        else:
            logging.error("Context argument is '" + context + "'. Only 'cpu' and 'gpu are valid arguments'.")

        gen_input_names = list(self._net_creator_gen.getInputs().keys())
        gen_input_names = [name[:-1] for name in gen_input_names]
        dis_input_names = list(self._net_creator_dis.getInputs().keys())
        dis_input_names = [name[:-1] for name in dis_input_names]
        if self.use_qnet:
            qnet_input_names = list(self._net_creator_qnet.getOutputs().keys())
            qnet_input_names = [name[:-1] for name in qnet_input_names]
        dis_real_input = list(self._net_creator_gen.getOutputs().keys())[0][:-1]

        gen_output_name = list(self._net_creator_gen.getOutputs().keys())[0][:-1]
        if self.use_qnet:
            cGAN_input_names = set(gen_input_names).difference(qnet_input_names)
            cGAN_input_names.discard(noise_input)
            cGAN_input_names = list(cGAN_input_names)
        else:
            cGAN_input_names = set(gen_input_names)
            cGAN_input_names.discard(noise_input)
            cGAN_input_names = list(cGAN_input_names)

        if preprocessing:
            preproc_lib = "CNNPreprocessor_infoGAN_infoGANConnector_predictor_executor"

        self._data_loader._output_names_ = []

        if not generator_target_name == "":
            self._data_loader._input_names_ = cGAN_input_names + [gen_output_name] + [generator_target_name]
        else:
            self._data_loader._input_names_ = cGAN_input_names + [gen_output_name]

        if preprocessing:
            train_iter, test_iter, data_mean, data_std, _, _ = self._data_loader.load_preprocessed_data(batch_size, preproc_lib)
        else:
            train_iter, test_iter, data_mean, data_std, _, _ = self._data_loader.load_data(batch_size)

        traindata_to_index = {}
        curIndex = 0
        for data_tuple in train_iter.data:
            traindata_to_index[data_tuple[0] + "_"] = curIndex
            curIndex += 1

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

        if 'weight_decay' in discriminator_optimizer_params:
            discriminator_optimizer_params['wd'] = discriminator_optimizer_params['weight_decay']
            del discriminator_optimizer_params['weight_decay']
        if 'learning_rate_decay' in optimizer_params:
            min_learning_rate = 1e-08
            if 'learning_rate_minimum' in discriminator_optimizer_params:
                min_learning_rate = discriminator_optimizer_params['learning_rate_minimum']
                del discriminator_optimizer_params['learning_rate_minimum']
            discriminator_optimizer_params['lr_scheduler'] = mx.lr_scheduler.FactorScheduler(
                                                   discriminator_optimizer_params['step_size'],
                                                   factor=discriminator_optimizer_params['learning_rate_decay'],
                                                   stop_factor_lr=min_learning_rate)
            del discriminator_optimizer_params['step_size']
            del discriminator_optimizer_params['learning_rate_decay']

        if normalize:
            self._net_creator_dis.construct([mx_context], batch_size=batch_size, data_mean=data_mean, data_std=data_std)
        else:
            self._net_creator_dis.construct([mx_context], batch_size=batch_size)

        self._net_creator_gen.construct([mx_context])

        if self.use_qnet:
            self._net_creator_qnet.construct([mx_context])
            if load_checkpoint:
                self._net_creator_qnet.load([mx_context])
            else:
                if os.path.isdir(self._net_creator_qnet._model_dir_):
                    shutil.rmtree(self._net_creator_qnet._model_dir_)
            try:
                os.makedirs(self._net_creator_qnet._model_dir_)
            except OSError:
                if not (os.path.isdir(self._net_creator_qnet._model_dir_)):
                    raise
            q_net = self._net_creator_qnet.networks[0]

        begin_epoch = 0
        if load_checkpoint:
            begin_epoch = self._net_creator_dis.load([mx_context])
            self._net_creator_gen.load([mx_context])
        else:
            if os.path.isdir(self._net_creator_dis._model_dir_):
                shutil.rmtree(self._net_creator_dis._model_dir_)
            if os.path.isdir(self._net_creator_gen._model_dir_):
                shutil.rmtree(self._net_creator_gen._model_dir_)

        dis_net = self._net_creator_dis.networks[0]
        gen_net = self._net_creator_gen.networks[0]

        try:
            os.makedirs(self._net_creator_gen._model_dir_)
            os.makedirs(self._net_creator_dis._model_dir_)
        except OSError:
            if not (os.path.isdir(self._net_creator_gen._model_dir_) and
                    os.path.isdir(self._net_creator_dis._model_dir_)):
                raise

        gen_trainer = mx.gluon.Trainer(gen_net.collect_params(), optimizer, optimizer_params)
        dis_trainer = mx.gluon.Trainer(dis_net.collect_params(), discriminator_optimizer, discriminator_optimizer_params)
        if self.use_qnet:
            qnet_trainer = mx.gluon.Trainer(q_net.collect_params(), discriminator_optimizer, discriminator_optimizer_params)

        dis_loss = mx.gluon.loss.SigmoidBinaryCrossEntropyLoss(from_sigmoid=True)
        dis_loss.hybridize()

        if not generator_loss == None:
            if generator_loss == "l2":
                generator_loss_func = mx.gluon.loss.L2Loss()
                generator_loss_func.hybridize()
            elif generator_loss == "l1":
                generator_loss_func = mx.gluon.loss.L1Loss()
                generator_loss_func.hybridize()
            else:
                logging.error("Invalid generator loss parameter")

        metric_dis = mx.metric.create(eval_metric)
        metric_gen = mx.metric.create(eval_metric)
        gen_inputs = self._net_creator_gen.getInputs()
        dis_inputs = self._net_creator_dis.getInputs()

        qnet_outputs = []
        if self.use_qnet:
            qnet_outputs = self._net_creator_qnet.getOutputs()
            qnet_losses = []
        generators = {}
        if self.use_qnet:
            for name in qnet_outputs:
                domain = gen_inputs[name]
                min = domain[1]
                max = domain[2]
                if name[:-1] in constraint_distributions:
                    dist_dict = constraint_distributions[name[:-1]]
                    dist_name = dist_dict['name']
                    if dist_name is "gaussian":
                        generators[name] = lambda domain=domain, min=min, max=max: mx.nd.cast(mx.ndarray.random.normal(dist_dict["mean_value"],
                                                                    dist_dict["spread_value"],
                                                                    shape=(batch_size,)+domain[3], dtype=domain[0],
                                                                    ctx=mx_context), dtype="float32")
                else:
                    if domain[0] == float:
                        generators[name] = lambda domain=domain, min=min, max=max: mx.nd.cast(mx.ndarray.random.uniform(min,max,
                                                    shape=(batch_size,)+domain[3],
                                                    dtype=domain[0], ctx=mx_context,), dtype="float32")
                    elif domain[0] == int:
                        generators[name] = lambda domain=domain, min=min, max=max:mx.ndarray.one_hot(mx.ndarray.random.randint(low=0,
                                                    high=int(max-min)+1, shape=(batch_size,), dtype=int,
                                                    ctx=mx_context), depth=int(max-min)+1, on_value=1).reshape((batch_size,)+domain[3])

                if name[-1] in constraint_losses:
                    loss_dict = constraint_losses[name[:-1]]
                    loss = loss_dict['name']
                    margin = loss_dict['margin'] if 'margin' in loss_dict else 1.0
                    sparseLabel = loss_dict['sparse_label'] if 'sparse_label' in loss_dict else True
                    ignore_indices = [loss_dict['ignore_indices']] if 'ignore_indices' in loss_dict else []
                    fromLogits = loss_dict['from_logits'] if 'from_logits' in loss_dict else False

                    if loss == 'softmax_cross_entropy':
                        qnet_losses += [mx.gluon.loss.SoftmaxCrossEntropyLoss(from_logits=fromLogits, sparse_label=sparseLabel)]
                    elif loss == 'softmax_cross_entropy_ignore_indices':
                        qnet_losses += [SoftmaxCrossEntropyLossIgnoreIndices(ignore_indices=ignore_indices, from_logits=fromLogits, sparse_label=sparseLabel)]
                    elif loss == 'sigmoid_binary_cross_entropy':
                        qnet_losses += [mx.gluon.loss.SigmoidBinaryCrossEntropyLoss(from_sigmoid=True)]
                    elif loss == 'cross_entropy':
                        qnet_losses += [CrossEntropyLoss(sparse_label=sparseLabel)]
                    elif loss == 'l2':
                        qnet_losses += [mx.gluon.loss.L2Loss()]
                    elif loss == 'l1':
                        qnet_losses += [mx.gluon.loss.L2Loss()]
                    elif loss == 'log_cosh':
                        qnet_losses += [LogCoshLoss()]
                    else:
                        logging.error("Invalid loss parameter for constraint:" + name[:-1] + ".")
                else:
                    if domain[0] == float:
                        qnet_losses += [mx.gluon.loss.L2Loss()]
                    elif domain[0] == int:
                        qnet_losses += [lambda pred, labels: mx.gluon.loss.SoftmaxCrossEntropyLoss(sparse_label=False)(pred, labels)]

        for name in gen_inputs:
            if name == noise_input + "_":
                domain = gen_inputs[name]
                min = domain[1]
                max = domain[2]
                if noise_distribution == "gaussian":
                    generators[name] = lambda domain=domain, min=min, max=max: mx.nd.cast(mx.ndarray.random.normal(noise_distribution_params["mean_value"],
                                                                noise_distribution_params["spread_value"],
                                                                shape=(batch_size,)+domain[3], dtype=domain[0],
                                                                ctx=mx_context), dtype="float32")
                elif noise_distribution == "uniform":
                    generators[name] = lambda domain=domain, min=min, max=max: mx.nd.cast(mx.ndarray.random.uniform(low=min,
                                                                high=max, shape=(batch_size,)+domain[3], dtype=domain[0],
                                                                ctx=mx_context), dtype="float32")

        def create_generator_input(cur_batch):
            expected_qnet_output = []
            gen_input = []

            for name in gen_inputs:
                if name in traindata_to_index.keys():
                    gen_input += [cur_batch.data[traindata_to_index[name]].as_in_context(mx_context)]
                elif name in qnet_outputs:
                    value = generators[name]()
                    expected_qnet_output += [value]
                    gen_input += [value]
                else:
                    gen_input += [generators[name]()]
            return gen_input, expected_qnet_output

        def create_discriminator_input(cur_batch):
            conditional_input = []
            for name in gen_inputs:
                if name in traindata_to_index.keys():
                    conditional_input += [cur_batch.data[traindata_to_index[name]].as_in_context(mx_context)]
            return conditional_input

        tic = None

        for epoch in range(begin_epoch, begin_epoch + num_epoch):
            train_iter.reset()
            for batch_i, batch in enumerate(train_iter):
                real_data = batch.data[traindata_to_index[dis_real_input + "_"]].as_in_context(mx_context)

                dis_conditional_input = create_discriminator_input(batch)
                gen_input, exp_qnet_output = create_generator_input(batch)

                with autograd.record():
                    fake_data = gen_net(*gen_input)[0][0]
                    fake_data.detach()
                    discriminated_fake_dis = dis_net(fake_data, *dis_conditional_input)[0][0]
                    if self.use_qnet:
                        discriminated_fake_dis, _ = discriminated_fake_dis

                    fake_labels = mx.nd.zeros(discriminated_fake_dis.shape, ctx=mx_context)
                    real_labels = mx.nd.ones(discriminated_fake_dis.shape, ctx=mx_context)

                    loss_resultF = dis_loss(discriminated_fake_dis, fake_labels)
                    discriminated_real_dis = dis_net(real_data, *dis_conditional_input)[0][0]
                    if self.use_qnet:
                        discriminated_real_dis, _ = discriminated_real_dis
                    loss_resultR = dis_loss(discriminated_real_dis, real_labels)

                    loss_resultD = dis_loss_weight * (loss_resultR + loss_resultF)
                    loss_resultD.backward()
                dis_trainer.step(batch_size)

                if batch_i % k_value == 0:
                    with autograd.record():
                        fake_data = gen_net(*gen_input)[0][0]
                        discriminated_fake_gen = dis_net(fake_data, *dis_conditional_input)[0][0]
                        if self.use_qnet:
                            discriminated_fake_gen, features = discriminated_fake_gen
                        loss_resultG = dis_loss(discriminated_fake_gen, real_labels)
                        if not generator_loss == None:
                            condition = batch.data[traindata_to_index[generator_target_name + "_"]]
                            loss_resultG = loss_resultG + gen_loss_weight * generator_loss_func(fake_data, condition)
                        if self.use_qnet:
                            qnet_discriminated = [q_net(features)[0][0]]
                            for i, qnet_out in enumerate(qnet_discriminated):
                                loss_resultG = loss_resultG + qnet_losses[i](qnet_out, exp_qnet_output[i])
                        loss_resultG.backward()
                    gen_trainer.step(batch_size)
                    if self.use_qnet:
                        qnet_trainer.step(batch_size)

                if tic is None:
                    tic = time.time()
                else:
                    if batch_i % log_period == 0:
                        try:
                            speed = log_period * batch_size / (time.time() - tic)
                        except ZeroDivisionError:
                            speed = float("inf")

                        logging.info(" Discriminator loss on real data: " + str(loss_resultR[0].asnumpy().item()))
                        logging.info(" Discriminator loss on fake data: " + str(loss_resultF[0].asnumpy().item()))
                        logging.info(" Generator loss: " + str(loss_resultG[0].asnumpy().item()))
                        logging.info("Epoch[%d] Batch[%d] Speed: %.2f samples/sec \n" % (epoch, batch_i, speed))

                        tic = time.time()

                        if print_images:
                            pyplot.subplot(1, 2, 1)
                            fake_img = fake_data[0]
                            visualize(fake_img)
                            filename = 'plot_%06d%06d.png' % (epoch, batch_i)
                            pyplot.savefig(filename)
                            pyplot.close()

            if (epoch - begin_epoch) % checkpoint_period == 0:
                    gen_net.save_parameters(self.parameter_path_gen() + '-' + str(epoch).zfill(4) + '.params')
                    dis_net.save_parameters(self.parameter_path_dis() + '-' + str(epoch).zfill(4) + '.params')

        gen_net.save_parameters(self.parameter_path_gen() + '-' + str(num_epoch + begin_epoch).zfill(4) + '.params')
        gen_net.export(self.parameter_path_gen() + '_newest', epoch=0)
        dis_net.save_parameters(self.parameter_path_dis() + '-' + str(num_epoch + begin_epoch).zfill(4) + '.params')
        dis_net.export(self.parameter_path_dis() + '_newest', epoch=0)
        if not generator_loss == None:
            generator_loss_func.export(self.parameter_path_gen() + '_newest_loss', epoch=0)
        dis_loss.export(self.parameter_path_dis() + '_newest_loss', epoch=0)

    def parameter_path_gen(self):
        return self._net_creator_gen._model_dir_ + self._net_creator_gen._model_prefix_ + '_' + str(0)

    def parameter_path_dis(self):
        return self._net_creator_dis._model_dir_ + self._net_creator_dis._model_prefix_ + '_' + str(0)
