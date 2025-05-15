import mxnet as mx
import logging
import os
import shutil
import warnings
import inspect
import sys

sys.path.insert(1, '/home/drakeman/mnistcalculator/gluon-cpp/src/python/gluon')
from custom_layers import *

from CNNNet_cNNCalculator_connector_predictor1 import Net_0

class CNNCreator_cNNCalculator_connector_predictor1:
    _model_dir_ = "model/cNNCalculator.Network/"
    _model_prefix_ = "model"

    def __init__(self):
        self.weight_initializer = mx.init.Normal()
        self.networks = {}
        self._weights_dir_ = None

    def load(self, context):
        earliestLastEpoch = None

        for i, network in self.networks.items():
            lastEpoch = 0
            param_file = None
            if hasattr(network, 'episodic_sub_nets'):
                num_episodic_sub_nets = len(network.episodic_sub_nets)
                lastMemEpoch = [0]*num_episodic_sub_nets
                mem_files = [None]*num_episodic_sub_nets

            try:
                os.remove(self._model_dir_ + self._model_prefix_ + "_" + str(i) + "_newest-0000.params")
            except OSError:
                pass
            try:
                os.remove(self._model_dir_ + self._model_prefix_ + "_" + str(i) + "_newest-symbol.json")
            except OSError:
                pass

            if hasattr(network, 'episodic_sub_nets'):
                try:
                    os.remove(self._model_dir_ + self._model_prefix_ + "_" + str(i) + '_newest_episodic_sub_net_' + str(0) + "-0000.params")
                except OSError:
                    pass
                try:
                    os.remove(self._model_dir_ + self._model_prefix_ + "_" + str(i) + '_newest_episodic_sub_net_' + str(0) + "-symbol.json")
                except OSError:
                    pass

                for j in range(len(network.episodic_sub_nets)):
                    try:
                        os.remove(self._model_dir_ + self._model_prefix_ + "_" + str(i) + '_newest_episodic_sub_net_' + str(j+1) + "-0000.params")
                    except OSError:
                        pass
                    try:
                        os.remove(self._model_dir_ + self._model_prefix_ + "_" + str(i) + '_newest_episodic_sub_net_' + str(j+1) + "-symbol.json")
                    except OSError:
                        pass
                    try:
                        os.remove(self._model_dir_ + self._model_prefix_ + "_" + str(i) + '_newest_episodic_query_net_' + str(j+1) + "-0000.params")
                    except OSError:
                        pass
                    try:
                        os.remove(self._model_dir_ + self._model_prefix_ + "_" + str(i) + '_newest_episodic_query_net_' + str(j+1) + "-symbol.json")
                    except OSError:
                        pass
                    try:
                        os.remove(self._model_dir_ + self._model_prefix_ + "_" + str(i) + '_newest_loss' + "-0000.params")
                    except OSError:
                        pass
                    try:
                        os.remove(self._model_dir_ + self._model_prefix_ + "_" + str(i) + '_newest_loss' + "-symbol.json")
                    except OSError:
                        pass
                    try:
                        os.remove(self._model_dir_ + self._model_prefix_ + "_" + str(i) + "_newest_episodic_memory_sub_net_" + str(j + 1) + "-0000")
                    except OSError:
                        pass

            if os.path.isdir(self._model_dir_):
                for file in os.listdir(self._model_dir_):
                    if ".params" in file and self._model_prefix_ + "_" + str(i) in file and not "loss" in file:
                        epochStr = file.replace(".params", "").replace(self._model_prefix_ + "_" + str(i) + "-", "")
                        epoch = int(epochStr)
                        if epoch >= lastEpoch:
                            lastEpoch = epoch
                            param_file = file
                    elif hasattr(network, 'episodic_sub_nets') and self._model_prefix_ + "_" + str(i) + "_episodic_memory_sub_net_" in file:
                        relMemPathInfo = file.replace(self._model_prefix_ + "_" + str(i) + "_episodic_memory_sub_net_", "").split("-")
                        memSubNet = int(relMemPathInfo[0])
                        memEpochStr = relMemPathInfo[1]
                        memEpoch = int(memEpochStr)
                        if memEpoch >= lastMemEpoch[memSubNet-1]:
                            lastMemEpoch[memSubNet-1] = memEpoch
                            mem_files[memSubNet-1] = file

            if param_file is None:
                earliestLastEpoch = 0
            else:
                logging.info("Loading checkpoint: " + param_file)
                network.load_parameters(self._model_dir_ + param_file)
                if hasattr(network, 'episodic_sub_nets'):
                    for j, sub_net in enumerate(network.episodic_sub_nets):
                        if mem_files[j] != None:
                            logging.info("Loading Replay Memory: " + mem_files[j])
                            mem_layer = [param for param in inspect.getmembers(sub_net, lambda x: not(inspect.isroutine(x))) if param[0].startswith("memory")][0][1]
                            mem_layer.load_memory(self._model_dir_ + mem_files[j])

                if earliestLastEpoch == None or lastEpoch + 1 < earliestLastEpoch:
                    earliestLastEpoch = lastEpoch + 1

        return earliestLastEpoch

    def load_pretrained_weights(self, context):
        if os.path.isdir(self._model_dir_):
            shutil.rmtree(self._model_dir_)
        if self._weights_dir_ is not None:
            for i, network in self.networks.items():
                # param_file = self._model_prefix_ + "_" + str(i) + "_newest-0000.params"
                param_file = None
                if hasattr(network, 'episodic_sub_nets'):
                    num_episodic_sub_nets = len(network.episodic_sub_nets)
                    lastMemEpoch = [0] * num_episodic_sub_nets
                    mem_files = [None] * num_episodic_sub_nets

                if os.path.isdir(self._weights_dir_):
                    lastEpoch = 0

                    for file in os.listdir(self._weights_dir_):

                        if ".params" in file and self._model_prefix_ + "_" + str(i) in file and not "loss" in file:
                            epochStr = file.replace(".params","").replace(self._model_prefix_ + "_" + str(i) + "-","")
                            epoch = int(epochStr)
                            if epoch >= lastEpoch:
                                lastEpoch = epoch
                                param_file = file
                        elif hasattr(network, 'episodic_sub_nets') and self._model_prefix_ + "_" + str(i) + "_episodic_memory_sub_net_" in file:
                            relMemPathInfo = file.replace(self._model_prefix_ + "_" + str(i) + "_episodic_memory_sub_net_").split("-")
                            memSubNet = int(relMemPathInfo[0])
                            memEpochStr = relMemPathInfo[1]
                            memEpoch = int(memEpochStr)
                            if memEpoch >= lastMemEpoch[memSubNet-1]:
                                lastMemEpoch[memSubNet-1] = memEpoch
                                mem_files[memSubNet-1] = file

                    logging.info("Loading pretrained weights: " + self._weights_dir_ + param_file)
                    network.load_parameters(self._weights_dir_ + param_file, allow_missing=True, ignore_extra=True)
                    if hasattr(network, 'episodic_sub_nets'):
                        assert lastEpoch == lastMemEpoch
                        for j, sub_net in enumerate(network.episodic_sub_nets):
                            if mem_files[j] != None:
                                logging.info("Loading pretrained Replay Memory: " + mem_files[j])
                                mem_layer = \
                                [param for param in inspect.getmembers(sub_net, lambda x: not (inspect.isroutine(x))) if
                                 param[0].startswith("memory")][0][1]
                                mem_layer.load_memory(self._model_dir_ + mem_files[j])
                else:
                    logging.info("No pretrained weights available at: " + self._weights_dir_ + param_file)

    def construct(self, context, data_mean=None, data_std=None):
        self.networks[0] = Net_0(data_mean=data_mean, data_std=data_std, mx_context=context, prefix="")
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            self.networks[0].collect_params().initialize(self.weight_initializer, force_reinit=False, ctx=context)
        self.networks[0].hybridize()
        self.networks[0](mx.nd.zeros((1, 1,28,28,), ctx=context[0]))

        if not os.path.exists(self._model_dir_):
            os.makedirs(self._model_dir_)

        for i, network in self.networks.items():
            network.export(self._model_dir_ + self._model_prefix_ + "_" + str(i), epoch=0)

    def setWeightInitializer(self, initializer):
        self.weight_initializer = initializer

    def getInputs(self):
        inputs = {}
        input_dimensions = (1,28,28,)
        input_domains = (int,0.0,255.0,)
        inputs["data_"] = input_domains + (input_dimensions,)
        return inputs

    def getOutputs(self):
        outputs = {}
        output_dimensions = (10,)
        output_domains = (float,0.0,1.0,)
        outputs["softmax_"] = output_domains + (output_dimensions,)
        return outputs

    def validate_parameters(self):

        pass