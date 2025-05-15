# (c) https://github.com/MontiCore/monticore
import importlib
import json
import logging, warnings
import os
import pathlib
import sys
import typing as t
from types import SimpleNamespace

import h5py
import logging
import warnings
import sys
import numpy as np
import importlib
import mxnet as mx
from mxnet import gluon, nd


from CNNDatasets_mnist_mnistClassifier_net import Dataset, TrainingDataset, RetrainingConf

class CNNDataLoader_mnist_mnistClassifier_net: # pylint: disable=invalid-name
    _input_names_ = ['image']
    _output_names_ = ['predictions_label']

    def __init__(self, data_cleaner):
        self._data_dir = "data/mnist.LeNetNetwork/"
        self._model_dir_   = os.path.join(os.path.join('./'), 'model', 'mnist.LeNetNetwork')
        self._data_cleaner = data_cleaner

    def load_data(self, batch_size, cleaning, cleaning_param, data_imbalance, data_imbalance_param, data_splitting, data_splitting_param , optimizer, shuffle=False, multi_graph=False):
        train_h5, test_h5, val_h5 = self.load_h5_files()

        if optimizer != 'hpo' and cleaning == 'remove': 
            if train_h5 is not None and test_h5 is not None:
                train_data, train_label = self._data_cleaner.clean_data( train_h5, cleaning_param )
                test_data, test_label = self._data_cleaner.clean_data( test_h5, cleaning_param )
            
                if data_imbalance == 'image_augmentation':
                    train_data, train_label = self._data_cleaner.image_augmentation( train_data, train_label, data_imbalance_param )
                    test_data, test_label = self._data_cleaner.image_augmentation( test_data, test_label, data_imbalance_param )
            else: 
                data_h5 = h5py.File(self._data_dir+"dataset.h5", "r")
                if cleaning == "remove":  
                    data, label = self._data_cleaner.clean_data( data_h5, cleaning_param )
                    if data_imbalance == 'image_augmentation': data, label = self._data_cleaner.image_augmentation( data, label, data_imbalance_param )
                    if data_splitting is not None: train_data, train_label, _, _, test_data, test_label = self._data_cleaner.tvt_split(data, label, data_splitting, data_splitting_param)
            train_h5, test_h5 = self._data_cleaner.numpy_to_hdf5(train_data, train_label, test_data, test_label, None, None)
            os.remove('_train.h5')
            os.remove('_test.h5')

        elif optimizer == 'hpo' and cleaning == 'remove':
            if train_h5 is not None and test_h5 is not None and val_h5 is not None and cleaning == 'remove':
                train_data, train_label = self._data_cleaner.clean_data( train_h5, cleaning_param )
                test_data, test_label = self._data_cleaner.clean_data( test_h5, cleaning_param )
                val_data, val_label = self._data_cleaner.clean_data( val_h5, cleaning_param )
            
                if data_imbalance == 'image_augmentation':
                    train_data, train_label = self._data_cleaner.image_augmentation( train_data, train_label, data_imbalance_param )
                    test_data, test_label = self._data_cleaner.image_augmentation( test_data, test_label, data_imbalance_param )
                    val_data, val_label = self._data_cleaner.image_augmentation( val_data, val_label, data_imbalance_param )
            else: 
                data_h5 = h5py.File(self._data_dir+"dataset.h5", "r")
                if cleaning == "remove":  
                    data, label = self._data_cleaner.clean_data( data_h5, cleaning_param ) 
                    if data_imbalance == 'image_augmentation': data, label = self._data_cleaner.image_augmentation( data, label, data_imbalance_param )
                    if data_splitting is not None: train_data, train_label, val_data, val_label, test_data, test_label = self._data_cleaner.tvt_split(data, label, data_splitting, data_splitting_param)
                
            train_h5, test_h5, val_h5 = self._data_cleaner.numpy_to_hdf5(train_data, train_label, test_data, test_label, val_data, val_label)
            os.remove('_train.h5')
            os.remove('_test.h5')
            os.remove('_validation.h5')

        if cleaning is not None:
            train_h5, val_h5, test_h5 = self._data_cleaner.get_cleaned_data(
                train_h5, val_h5, test_h5, 
                optimizer,
                cleaning, cleaning_param, 
                data_imbalance, data_imbalance_param, 
                data_splitting, data_splitting_param
            )

        train_graph = None
        test_graph = None
        train_data = {}
        data_mean = {}
        data_std = {}
        train_images = {}
        test_images = {}

        for input_name in self._input_names_:
            if input_name in train_h5:
                train_data["input"+str(self._input_names_.index(input_name))] = train_h5[input_name]
                train_dataset = train_h5[input_name]
                train_dataset_shape = train_data["input"+str(self._input_names_.index(input_name))].shape
                # slice_size limits the memory consumption, by only loading slices of size <500MB into memory
                slice_size = min(max(train_dataset_shape[0] - 1, 1), int(500e6 / (train_h5[input_name][0].size * \
                    train_h5[input_name][0].itemsize)))
                num_slices = max(1, int(train_h5[input_name].shape[0] / slice_size))
                mean = np.zeros(train_dataset_shape[1: ])
                std = np.zeros(train_dataset_shape[1: ])

                for i in range(int(train_dataset_shape[0] / slice_size)):
                    mean += train_dataset[i * slice_size: (i + 1) * slice_size].mean(axis=0) / num_slices
                    std += train_dataset[i * slice_size: (i + 1) * slice_size].std(axis=0) / num_slices
                if slice_size > train_dataset_shape[0] - 1:
                    mean += train_dataset[num_slices * slice_size: ].mean(axis=0) / (slice_size - num_slices % slice_size)
                    std += train_dataset[num_slices * slice_size: ].std(axis=0) / (slice_size - num_slices % slice_size)
                std += 1e-5

                data_mean[input_name + '_'] = nd.array(mean)
                data_std[input_name + '_'] = nd.array(std)

                if 'images' in train_h5:
                    train_images = train_h5['images']

        train_label = {}
        index = 0
        for output_name in self._output_names_:
            train_label[index] = train_h5[output_name]
            index += 1
        if len(train_data) == 0:
            train_data = train_label
        if multi_graph:
            batch_handle = 'discard'
        else:
            batch_handle = 'pad'
        train_iter = mx.io.NDArrayIter(data=train_data,
                                       label=train_label,
                                       batch_size=batch_size,
                                       shuffle=shuffle,
                                       last_batch_handle=batch_handle)
        test_iter = None

        if test_h5 != None:
            test_data = {}
            test_images = {}
            for input_name in self._input_names_:
                if input_name in test_h5:
                    test_data["input"+str(self._input_names_.index(input_name))] = test_h5[input_name]

                    if 'images' in test_h5:
                        test_images = test_h5['images']

            test_label = {}
            index = 0
            for output_name in self._output_names_:
                test_label[index] = test_h5[output_name]
                index += 1
            if len(test_data) == 0:
                test_data = test_label
            test_iter = mx.io.NDArrayIter(data=test_data,
                                          label=test_label,
                                          batch_size=batch_size,
                                          last_batch_handle=batch_handle)

        val_iter = None

        if val_h5 != None:
            val_data = {}
            val_images = {}
            for input_name in self._input_names_:
                if input_name in val_h5:
                    val_data["input"+str(self._input_names_.index(input_name))] = val_h5[input_name]

                    if 'images' in val_h5:
                        val_images = val_h5['images']
                else:
                    if input_name == 'graph':
                        if multi_graph:
                            val_graph, _ = load_graphs(self._data_dir + "test" + "_graph")

            val_label = {}
            index = 0
            for output_name in self._output_names_:
                val_label[index] = val_h5[output_name]
                index += 1
            if len(val_data) == 0:
                val_data = val_label
            val_iter = mx.io.NDArrayIter(data=val_data,
                                        label=val_label,
                                        batch_size=batch_size,
                                        last_batch_handle=batch_handle)

        return train_iter, test_iter, val_iter, data_mean, data_std, train_images, test_images, train_graph, test_graph

    
    def load_preprocessed_data(self, batch_size, preproc_lib, shuffle=False):
        train_h5, test_h5, val_h5 = self.load_h5_files()
        
        wrapper = importlib.import_module(preproc_lib)
        instance = getattr(wrapper, preproc_lib)()
        instance.init()
        lib_head, _sep, tail = preproc_lib.rpartition('_')
        inp = getattr(wrapper, lib_head + "_input")()

        train_data = {}
        train_label = {}
        data_mean = {}
        data_std = {}
        train_images = {}

        shape_output = self.preprocess_data(instance, inp, 0, train_h5)
        train_len = len(train_h5[self._input_names_[0]])

        for input_name in self._input_names_:
            if type(getattr(shape_output, input_name + "_out")) == np.ndarray:
                cur_shape = (train_len,) + getattr(shape_output, input_name + "_out").shape
            else:
                cur_shape = (train_len, 1)
            train_data["input"+str(self._input_names_.index(input_name))] = mx.nd.zeros(cur_shape)
        for output_name in self._output_names_:
            if type(getattr(shape_output, output_name + "_out")) == nd.array:
                cur_shape = (train_len,) + getattr(shape_output, output_name + "_out").shape
            else:
                cur_shape = (train_len, 1)
            train_label[output_name] = mx.nd.zeros(cur_shape)

        for i in range(train_len):
            output = self.preprocess_data(instance, inp, i, train_h5)
            for input_name in self._input_names_:
                train_data["input"+str(self._input_names_.index(input_name))][i] = getattr(output, input_name + "_out")
            for output_name in self._output_names_:
                train_label[output_name][i] = getattr(shape_output, output_name + "_out")

        for input_name in self._input_names_:
            data_mean[input_name + '_'] = nd.array(train_data["input"+str(self._input_names_.index(input_name))][:].mean(axis=0))
            data_std[input_name + '_'] = nd.array(train_data["input"+str(self._input_names_.index(input_name))][:].asnumpy().std(axis=0) + 1e-5)

        if 'images' in train_h5:
            train_images = train_h5['images']

        train_iter = mx.io.NDArrayIter(data=train_data,
                                       label=train_label,
                                       batch_size=batch_size,
                                       shuffle=shuffle)

        test_data = {}
        test_label = {}

        shape_output = self.preprocess_data(instance, inp, 0, test_h5)
        test_len = len(test_h5[self._input_names_[0]])

        for input_name in self._input_names_:
            if type(getattr(shape_output, input_name + "_out")) == np.ndarray:
                cur_shape = (test_len,) + getattr(shape_output, input_name + "_out").shape
            else:
                cur_shape = (test_len, 1)
            test_data["input"+str(self._input_names_.index(input_name))] = mx.nd.zeros(cur_shape)
        for output_name in self._output_names_:
            if type(getattr(shape_output, output_name + "_out")) == nd.array:
                cur_shape = (test_len,) + getattr(shape_output, output_name + "_out").shape
            else:
                cur_shape = (test_len, 1)
            test_label[output_name] = mx.nd.zeros(cur_shape)

        for i in range(test_len):
            output = self.preprocess_data(instance, inp, i, test_h5)
            for input_name in self._input_names_:
                test_data["input"+str(self._input_names_.index(input_name))][i] = getattr(output, input_name + "_out")
            for output_name in self._output_names_:
                test_label[output_name][i] = getattr(shape_output, output_name + "_out")

        test_images = {}
        if 'images' in test_h5:
            test_images = test_h5['images']

        test_iter = mx.io.NDArrayIter(data=test_data,
                                       label=test_label,
                                       batch_size=batch_size)

        return train_iter, test_iter, data_mean, data_std, train_images, test_images

    def preprocess_data(self, instance_wrapper, input_wrapper, index, data_h5):
        for input_name in self._input_names_:
            data = data_h5[input_name][index]
            attr = getattr(input_wrapper, input_name)
            if (type(data)) == np.ndarray:
                data = np.asfortranarray(data).astype(attr.dtype)
            else:
                data = type(attr)(data)
            setattr(input_wrapper, input_name, data)
        for output_name in self._output_names_:
            data = data_h5[output_name][index]
            attr = getattr(input_wrapper, output_name)
            if (type(data)) == np.ndarray:
                data = np.asfortranarray(data).astype(attr.dtype)
            else:
                data = type(attr)(data)
            setattr(input_wrapper, output_name, data)
        return instance_wrapper.execute(input_wrapper)

    def load_h5_files(self, learning_method="", dataset: pathlib.Path = None, val_dataset: t.Optional[str] = None, test_dataset: t.Optional[str] = None):
        if not dataset:
            dataset = self._data_dir / "train.h5"
        train_h5 = self.load_dataset(dataset, learning_method)
        test_h5 = None
        val_h5 = None
        train_path = self._data_dir + "train.h5"
        test_path = self._data_dir + "test.h5"
        val_path = self._data_dir + "validation.h5"

        if os.path.isfile(train_path):
            train_h5 = h5py.File(train_path, 'r')
            for input_name in self._input_names_:
                if not input_name in train_h5:
                    logging.error("The HDF5 file '" + os.path.abspath(train_path) + "' has to contain the dataset "
                                  + "'" + input_name + "'")
            if learning_method != "vae":
                for output_name in self._output_names_:
                    if not output_name in train_h5:
                        logging.error("The HDF5 file '" + os.path.abspath(train_path) + "' has to contain the dataset "
                                      + "'" + output_name + "'")

        if os.path.isfile(test_path):
            test_h5 = h5py.File(test_path, 'r')
            for input_name in self._input_names_:
                if not input_name in test_h5:
                    logging.error("The HDF5 file '" + os.path.abspath(test_path) + "' has to contain the dataset "
                                    + "'" + input_name + "'")
            if learning_method != "vae":
                for output_name in self._output_names_:
                    if not output_name in test_h5:
                        logging.error("The HDF5 file '" + os.path.abspath(test_path) + "' has to contain the dataset "
                                        + "'" + output_name + "'")

        if os.path.isfile(val_path):
            val_h5 = h5py.File(val_path, 'r')
            for input_name in self._input_names_:
                if not input_name in val_h5:
                    logging.error("The HDF5 file '" + os.path.abspath(val_path) + "' has to contain the dataset "
                                    + "'" + input_name + "'")
            if learning_method != "vae":
                for output_name in self._output_names_:
                    if not output_name in val_h5:
                        logging.error("The HDF5 file '" + os.path.abspath(val_path) + "' has to contain the dataset "
                                        + "'" + output_name + "'")


        return train_h5, test_h5, val_h5

    def load_vae_data(self, batch_size, shuffle=False, input_names=[] ):
        self._input_names_ = input_names
        train_h5, test_h5, val_h5 = self.load_h5_files(learning_method="vae")

        train_data = {}
        data_mean = {}
        data_std = {}
        train_images = {}
        train_label = {}

        for input_name in self._input_names_:
            if input_name == "label":
                train_label[input_name] = train_h5[input_name]
            else:
                train_data[input_name] = train_h5[input_name]
                train_dataset = train_h5[input_name]
                train_dataset_shape = train_data[input_name].shape
                # slice_size limits the memory consumption, by only loading slices of size <500MB into memory
                slice_size = min(train_dataset_shape[0] - 1, int(500e6 / (train_h5[input_name][0].size * \
                train_h5[input_name][0].itemsize)))
                num_slices = max(1, int(train_h5[input_name].shape[0] / slice_size))
                mean = np.zeros(train_dataset_shape[1:])
                std = np.zeros(train_dataset_shape[1:])

                for i in range(int(train_dataset_shape[0] / slice_size)):
                    mean += train_dataset[i * slice_size: (i + 1) * slice_size].mean(axis=0) / num_slices
                    std += train_dataset[i * slice_size: (i + 1) * slice_size].std(axis=0) / num_slices
                if slice_size > train_dataset_shape[0] - 1:
                    mean += train_dataset[num_slices * slice_size:].mean(axis=0) / (
                    slice_size - num_slices % slice_size)
                    std += train_dataset[num_slices * slice_size:].std(axis=0) / (slice_size - num_slices % slice_size)
                std += 1e-5

                data_mean[input_name + '_'] = nd.array(mean)
                data_std[input_name + '_'] = nd.array(std)

        if 'images' in train_h5:
            train_images = train_h5['images']

        train_iter = mx.io.NDArrayIter(data=train_data,
                                       label=train_label,
                                       batch_size=batch_size,
                                       shuffle=shuffle)

        test_iter = None

        if test_h5 is not None:
            test_data = {}
            test_label = {}
            test_images = {}
            for input_name in self._input_names_:
                if input_name == "label":
                    test_label[input_name] = test_h5[input_name]
                else:
                    test_data[input_name] = test_h5[input_name]

            if 'images' in test_h5:
                test_images = test_h5['images']


            test_iter = mx.io.NDArrayIter(data=test_data,
                                          label=test_label,
                                          batch_size=batch_size)

        return train_iter, test_iter, data_mean, data_std, train_images, test_images

    def check_bias(self):
        _, test, _ = self.load_h5_files()
        data, label = np.array(test["data"]), np.array(test["softmax_label"]).astype(int)

        # load model 
        ctx = mx.gpu() if mx.context.num_gpus() else mx.cpu()
        with warnings.catch_warnings():
            warnings.simplefilter("ignore")
            net = gluon.nn.SymbolBlock.imports(
                self._model_dir_ + '/model_0_newest-symbol.json',
                ["data"],
                self._model_dir_ + '/model_0_newest-0000.params',
                ctx=ctx
            )

        # calculate confusion matrix
        CM = np.zeros((10,10), dtype=int)
        for t in range(len(data)):
            i = label[t]
            out = net(nd.array([data[t]]).as_in_context(ctx))
            j = int(nd.argmax(out, axis=1).asnumpy()[0]) # get predicted result
            CM[i][j] += 1

        # check number of matrix dimensions
        sum_col = np.sum(CM, axis=0, dtype=np.int32)
        sum_row = np.sum(CM, axis=1, dtype=np.int32)
        if(np.sum(sum_col) != np.sum(sum_col)):
            logging.error("Sum of actual label and sum of predicted label is different in Confusion Matrix!")

        # start Bias Checking
        TP = CM.diagonal()
        
        TPR = np.zeros((10), dtype=float)
        for c in range(10):
            TPR[c] = np.round(CM[c][c] / (CM[c][c] + sum([CM[c][i] for i in range(0,10) if i != c])),4)

        FPR = np.zeros((10), dtype=float)
        for c in range(10):
            FP = sum([CM[i][c] for i in range(0,10) if i != c])
            FPR[c] = np.round(FP / (FP + sum([CM[i][j] for i in range(0,10) for j in range(0,10) if i != c])),4)

        # show results
        logging.info('\n--------- Check Model Bias: --------------------------------------------------\n')
        
        logging.info('TP:' + np.array2string(TP))
        logging.info('TPR:' + np.array2string(TPR))
        logging.info('FPR:' + np.array2string(FPR))

        logging.info('\n\tSatisfy Multi-class Demographic Parity \t...... ' + str(np.all(np.isclose(TP, TP[0]))))
        logging.info('\tSatisfy Multi-class Equality of Odds \t...... ' + str(np.all(np.isclose(TPR, TPR[0])) and np.all(np.isclose(FPR, FPR[0]))))
        logging.info('\tSatisfy Multi-class Equal Opportunity \t...... ' + str(np.all(np.isclose(TPR, TPR[0]))))

        logging.info('\n------------------------------------------------------------------------------')

