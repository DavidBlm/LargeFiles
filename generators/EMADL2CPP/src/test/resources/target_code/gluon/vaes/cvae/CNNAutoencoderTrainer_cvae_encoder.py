# (c) https://github.com/MontiCore/monticore
import os
import shutil
import sys
import warnings

import mxnet as mx
import logging
import time
import numpy as np
import matplotlib.pyplot as plt
from mxnet import nd, autograd, gluon
from mxnet.gluon import nn

class VAELoss(gluon.loss.Loss):
    def __init__(self, weight=None, batch_axis=0, recon_loss='mse', kl_loss_weight=1, loss_ctx_list=[], **kwargs):
        super(VAELoss, self).__init__(weight,batch_axis,**kwargs)
        self.soft_zero = 1e-10
        self.kl_weight = kl_loss_weight
        self.loss_ctx_list = loss_ctx_list
        self.recon_loss = recon_loss

    def hybrid_forward(self, F, pred, data, loss_param_list=None):
        x = data
        y = pred
        loss = 0

        # Calculate the regulariazation terms with the Layer parameters
        if len(self.loss_ctx_list) != 0:
            for i in range(len(self.loss_ctx_list)):
                dict = self.loss_ctx_list[i]
                loss_params = loss_param_list[i]
                val_dict = dict["values"]
                if dict["loss"] == "kl_div_loss":
                    pdf = val_dict["pdf"]
                    loss = - self.kl_loss(F, pdf, loss_params, self.kl_weight) + loss
                elif dict["loss"] == "quantization_loss":
                    commitment_weight = val_dict['beta']

                    # Calculate vector quantization loss
                    commitment_loss = commitment_weight * loss_params[0]
                    codebook_loss = loss_params[1]
                    loss = commitment_loss + codebook_loss + loss

        # Reconstruction Loss
        if self.recon_loss == "bce":                #Binary data
            reconstruction_loss = - F.sum(x * F.log(y + self.soft_zero) + (1 - x) * F.log(1 - y + self.soft_zero),  axis=0, exclude=True)
        elif self.recon_loss == "mse":              #Continous data
            reconstruction_loss = F.sum((x - y)**2,  axis=0, exclude=True)
        loss = reconstruction_loss + loss
        return [loss, reconstruction_loss]

    def kl_loss(self, F, pdf, params, weight=1):
        kl = 0
        #Calculates the KL-Divergence between two Distr. that are given by the Layerparameter 'pdf' from Reparameterize
        if pdf == "normal":
            # KL-Div between two gaussians
            mu = params[0]
            logvar = params[1]
            kl = weight * 0.5 * F.sum(1 + logvar - mu * mu - F.exp(logvar), axis=0, exclude=True)

        return kl


class CNNAutoencoderTrainer:
    def __init__(self, data_loader, encoder_net_constructor, decoder_net_constructor):
        self._data_loader = data_loader
        self._enc_creator = encoder_net_constructor
        self._dec_creator = decoder_net_constructor

    def train(self, batch_size=64,
              num_epoch=10,
              optimizer='adam',
              optimizer_params=(('learning_rate', 0.001),),
              load_checkpoint=False,
              context='cpu',
              reconstruction_loss='mse',
              preprocessing=False, #TODO
              checkpoint_period=5,
              load_pretrained=False,
              normalize=False,
              log_period = 50,
              kl_loss_weight=1,
              print_images=False):
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
                sys.exit(1)
        elif context == 'cpu':
            mx_context = [mx.cpu()]
        else:
            logging.error("Context argument is '" + context + "'. Only 'cpu' and 'gpu are valid arguments'.")
            sys.exit(1)

        single_pu_batch_size = int(batch_size / num_pus)

        if print_images:
            try:
                logging.info("Creating 'images' directory...")
                if not os.path.isdir('images'):
                    os.mkdir('images')
                    logging.info("Created 'images' directory.")
                else:
                    logging.info("'images' directory already exists.")
            except:
                logging.error("Creation of the 'images' directory failed.")

        input_names = [ "data", "label"]
        train_iter, test_iter, data_mean, data_std, _, _ = self._data_loader.load_vae_data(batch_size=batch_size, input_names=input_names, shuffle=True)

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
                                                  base_lr=optimizer_params['learning_rate'],
                                                  factor=optimizer_params['learning_rate_decay'],
                                                  stop_factor_lr=min_learning_rate)
            del optimizer_params['step_size']
            del optimizer_params['learning_rate_decay']

        begin_epoch = 0

        if load_checkpoint:
            begin_epoch = self._enc_creator.load(mx_context)
            _ = self._dec_creator.load(mx_context)
        elif load_pretrained:
            self._enc_creator.load_pretrained_weights(mx_context)
            self._dec_creator.load_pretrained_weights(mx_context)
        else:
            if os.path.isdir(self._enc_creator._model_dir_):
                shutil.rmtree(self._enc_creator._model_dir_)
            if os.path.isdir(self._dec_creator._model_dir_):
                shutil.rmtree(self._dec_creator._model_dir_)

        if normalize:
            self._enc_creator.construct(context=mx_context, batch_size=batch_size, data_mean=data_mean, data_std=data_std)
            self._dec_creator.construct(context=mx_context, batch_size=batch_size, data_mean=data_mean, data_std=data_std)
        else:
            self._enc_creator.construct(context=mx_context, batch_size=batch_size)
            self._dec_creator.construct(context=mx_context, batch_size=batch_size)

        encoder_nets = self._enc_creator.networks
        decoder_nets = self._dec_creator.networks

        if len(encoder_nets) > 1:
            logging.error("VAE-components don't support multiple networkmodels yet. Encoder-Networks found: " + str(len(encoder_nets)))
            sys.exit(1)
        elif len(decoder_nets) > 1:
            logging.error("VAE-components don't support multiple networkmodels yet. Decoder-Networks found: " + str(len(decoder_nets)))
            sys.exit(1)

        #Loss parameters from layers
        loss_ctx_list = []

        loss_ctx_list.append(encoder_nets[0].loss_ctx_dict)
        


        enc_trainers = [mx.gluon.Trainer(network.collect_params(), optimizer, optimizer_params) for network in
                    encoder_nets.values() if len(network.collect_params().values()) != 0]
        dec_trainers = [mx.gluon.Trainer(network.collect_params(), optimizer, optimizer_params) for network in
                    decoder_nets.values() if len(network.collect_params().values()) != 0]

        #Calculates the Loss dependent on the modeled architecture
        loss_function = VAELoss(recon_loss=reconstruction_loss, kl_loss_weight=kl_loss_weight, loss_ctx_list=loss_ctx_list)

        loss_function.hybridize()

        tic = None

        avg_speed = 0
        n = 0

        train_lost_list = []
        test_lost_list = []

        for epoch in range(begin_epoch, begin_epoch + num_epoch):
            global_loss_train = 0.0
            global_reconloss = 0.0
            train_batches = 0

            loss_total = 0
            recon_total = 0
            train_iter.reset()

            #Using training dataset
            for batch_i, batch in enumerate(train_iter):

                with autograd.record():
                    indexed_labels = 0
                    indexed_data = 0
                    if "data" == "label":
                        data_ = gluon.utils.split_and_load(batch.label[indexed_labels], ctx_list=mx_context, even_split=False)
                        indexed_labels += 1
                    else:
                        data_ = gluon.utils.split_and_load(batch.data[indexed_data], ctx_list=mx_context, even_split=False)
                        indexed_data += 1
                    if "label" == "label":
                        label_ = gluon.utils.split_and_load(batch.label[indexed_labels], ctx_list=mx_context, even_split=False)
                        indexed_labels += 1
                    else:
                        label_ = gluon.utils.split_and_load(batch.data[indexed_data], ctx_list=mx_context, even_split=False)
                        indexed_data += 1

                    #Lists to keep data during Multi-GPU Training
                    lossList = []
                    loss_param_list = []
                    reconstruction_losses = []
                    encoding_ = []
                    pred_= []

                    for i in range(num_pus):
                        lossList.append([])
                        loss_param_list.append([])
                        reconstruction_losses.append([])
                        encoding_.append([])
                        pred_.append([])

                    #Feed-Forward - Training
                    nd.waitall()
                    for i in range(num_pus):
                        feature_vec, loss_params_enc = encoder_nets[0]( data_[i], label_[i])
                        loss_param_list[i].append(loss_params_enc)
                        encoding_[i] = feature_vec[0]

                    nd.waitall()
                    for i in range(num_pus):
                        res_ = decoder_nets[0]( encoding_[i],  label_[i])
                        
                        pred_[i] = res_[0][0]

                    nd.waitall()
                    for i in range(num_pus):
                        elbo, reconstruction_loss = loss_function(pred_[i], data_[i], loss_param_list[i])
                        lossList[i].append(elbo)
                        reconstruction_losses[i].append(reconstruction_loss)


                    losses = [0] * num_pus
                    reconLosses = [0] * num_pus
                    for i in range(num_pus):
                        for element in lossList[i]:
                            losses[i] = losses[i] + element
                        for r in reconstruction_losses[i]:
                            reconLosses[i] = reconLosses[i] + r

                for loss in losses:
                    loss.backward()
                    loss_total += loss.sum().asscalar()
                    global_loss_train += loss.sum().asscalar()

                for loss in reconLosses:
                    recon_total += loss.sum().asscalar()
                    global_reconloss += loss.sum().asscalar()

                train_batches += 1

                for trainer in dec_trainers:
                    trainer.step(batch_size)
                for trainer in enc_trainers:
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
                        recon_avg = recon_total / (batch_size * log_period)
                        loss_total = 0
                        recon_total = 0

                        logging.info("Epoch[%d] Batch[%d] Speed: %.2f samples/sec Average Negative-ELBO Loss: %.5f, Reconstruction Loss: %.5f" % (
                        epoch, batch_i, speed, loss_avg, recon_avg))

                        avg_speed += speed
                        n += 1

                        tic = time.time()


            global_loss_train /= (train_batches * batch_size)
            global_reconloss /= (train_batches * batch_size)

            tic = None

            global_loss_test = 0.0
            test_batches = 0

            test_iter.batch_size = single_pu_batch_size
            test_iter.reset()

            #Using test dataset
            for batch_i, batch in enumerate(test_iter):

                indexed_labels = 0
                indexed_data = 0
                if "data" == "label":
                    data_ = gluon.utils.split_and_load(batch.label[indexed_labels], ctx_list=mx_context, even_split=False)
                    indexed_labels += 1
                else:
                    data_ = gluon.utils.split_and_load(batch.data[indexed_data], ctx_list=mx_context, even_split=False)
                    indexed_data += 1
                if "label" == "label":
                    label_ = gluon.utils.split_and_load(batch.label[indexed_labels], ctx_list=mx_context, even_split=False)
                    indexed_labels += 1
                else:
                    label_ = gluon.utils.split_and_load(batch.data[indexed_data], ctx_list=mx_context, even_split=False)
                    indexed_data += 1

                lossList = []
                loss_param_list = []
                encoding_ = []
                pred_= []

                for i in range(num_pus):
                    lossList.append([])
                    loss_param_list.append([])
                    encoding_.append([])
                    pred_.append([])

                #Feed-Forward - Testing
                num_test_images = 5
                nd.waitall()
                for i in range(num_pus):
                    feature_vec, loss_params_enc = encoder_nets[0]( data_[i][:num_test_images], label_[i][:num_test_images])
                    loss_param_list[i].append(loss_params_enc)
                    encoding_[i] = feature_vec[0]

                nd.waitall()
                for i in range(num_pus):
                    res_ = decoder_nets[0]( encoding_[i][:num_test_images],  label_[i][:num_test_images])
                    
                    pred_[i] = res_[0][0]

                nd.waitall()
                for i in range(num_pus):
                    elbo, reconstruction_loss = loss_function(pred_[i], data_[i][:num_test_images], loss_param_list[i])
                    lossList[i].append(elbo)

                losses = [0] * num_pus
                for i in range(num_pus):
                    for element in lossList[i]:
                        losses[i] = losses[i] + element

                for loss in losses:
                    global_loss_test += loss.sum().asscalar()

                test_batches += 1

            global_loss_test /= (test_batches * num_test_images)

            logging.info("Epoch[%d], Epoch Train Loss: %f, Epoch Reconstruction Loss: %f, Validation Loss: %f" % (
                epoch, global_loss_train, global_reconloss, global_loss_test))

            if (epoch+1) % checkpoint_period == 0:
                for i, network in encoder_nets.items():
                    if network.save_specific_params_list:
                        for name, param_dic in network.save_specific_params_list:
                            param_dic.save(self.encoder_parameter_path(i) + '-' + name + '-' + str(epoch).zfill(4) + '.params')
                    #Not sure if actually needed. Keeping it because CNNSupervised has it. Had problems reloading parameters with this.
                    #network.save_parameters(self.encoder_parameter_path(i) + '-' + str(epoch).zfill(4) + '.params')
                    network.export(self.encoder_parameter_path(i), epoch=epoch)
                for i, network in decoder_nets.items():
                    if network.save_specific_params_list:
                        for name, param_dic in network.save_specific_params_list:
                            param_dic.save(self.decoder_parameter_path(i) + '-' + name + '-' + str(epoch).zfill(4) + '.params')
                    #Not sure if actually needed. Keeping it because CNNSupervised has it. Had problems reloading parameters with this.
                    #network.save_parameters(self.decoder_parameter_path(i) + '-' + str(epoch).zfill(4) + '.params')
                    network.export(self.decoder_parameter_path(i), epoch=epoch)

            if print_images:
                train_lost_list.append(global_loss_train)
                test_lost_list.append(global_loss_test)

                try:
                    for i in range(num_test_images):
                        # Reconstructions
                        filename = 'test_reconstruction_%d_epoch%d_batch_size%d.png' % (i,epoch, batch_size)
                        fig = plt.figure()
                        ax = fig.add_subplot(1, 2, 1)
                        plt.imshow(data_[0][i].squeeze(0).asnumpy())
                        ax.set_title('Original')
                        ax = fig.add_subplot(1, 2, 2)
                        plt.imshow(pred_[0][i].squeeze(0).asnumpy())
                        ax.set_title('Reconstruction')
                        plt.tight_layout()
                        plt.savefig('images/' + filename)
                        plt.close()
                except:
                    logging.info("Could not print reconstruction images.")

        if print_images:
            if num_epoch != 1:
                try:
                    #Loss plot
                    batch_x = np.linspace(1, num_epoch, len(train_lost_list))
                    filename = 'loss_graph.png'
                    plt.plot(batch_x, np.array(train_lost_list))
                    plt.plot(batch_x, np.array(test_lost_list))
                    plt.legend(['Train loss', 'Validation Loss'])
                    plt.savefig('images/' + filename)
                except:
                    logging.info("Could not print loss plot image.")

        for i, network in encoder_nets.items():
            if network.save_specific_params_list:
                for name, param_dic in network.save_specific_params_list:
                    param_dic.save(self.encoder_parameter_path(i) + '-' + name + '-' + str((num_epoch-1) + begin_epoch).zfill(4) +'.params')
            #Not sure if actually needed. Keeping it because CNNSupervised has it. Had problems reloading parameters with this.
            #network.save_parameters(self.encoder_parameter_path(i) + '-' + str((num_epoch-1) + begin_epoch).zfill(4) + '.params')
            network.export(self.encoder_parameter_path(i) + '_newest', epoch=0)

            loss_function.export(self.encoder_parameter_path(i) + '_newest_loss', epoch=0)

        for i, network in decoder_nets.items():
            if network.save_specific_params_list:
                for name, param_dic in network.save_specific_params_list:
                    param_dic.save(self.decoder_parameter_path(i) + '-' + name + '-' + str((num_epoch-1) + begin_epoch).zfill(4) +'.params')
            #Not sure if actually needed. Keeping it because CNNSupervised has it. Had problems reloading parameters with this.
            #network.save_parameters(self.decoder_parameter_path(i) + '-' + str((num_epoch-1) + begin_epoch).zfill(4) + '.params')
            network.export(self.decoder_parameter_path(i) + '_newest', epoch=0)

            loss_function.export(self.decoder_parameter_path(i) + '_newest_loss', epoch=0)

    def encoder_parameter_path(self, index):
        return self._enc_creator._model_dir_ + self._enc_creator._model_prefix_ + '_' + str(index)

    def decoder_parameter_path(self, index):
        return self._dec_creator._model_dir_ + self._dec_creator._model_prefix_ + '_' + str(index)

