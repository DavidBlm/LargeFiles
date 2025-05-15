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
import numpy as np
import mxnet as mx
from mxnet import gluon, nd
from dgl.data.utils import load_graphs

from CNNDatasets_coraDgl_dGLNetwork import Dataset, TrainingDataset, RetrainingConf

class CNNDataLoader_coraDgl_dGLNetwork: # pylint: disable=invalid-name
    _input_names_ = ['graph', 'features', ]
    _output_names_ = ['predictions_label']

    def __init__(self, data_cleaner):
        self._data_dir = pathlib.Path("src/test/resources/training_data/GNN/CoraDgl/")
        self._model_dir_   = os.path.join(os.path.join('./'), 'model', 'coraDgl.DGLNetwork')
        self._data_cleaner = data_cleaner

    def load_data(
            self, batch_size, cleaning, cleaning_param, data_imbalance, data_imbalance_param, data_splitting, data_splitting_param , optimizer,
            shuffle=False, multi_graph=False, dataset: TrainingDataset=None, test_dataset: Dataset=None, val_dataset: Dataset=None
    ):
        if not dataset:
            raise KeyError("No dataset specified.")

        train_h5, val_h5, test_h5 = self.load_h5_files(
            "",
            pathlib.Path(dataset.path),
            pathlib.Path(val_dataset.path) if val_dataset else None,
            pathlib.Path(test_dataset.path) if test_dataset else None
        )

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
            else:
                if input_name == 'graph':
                    if multi_graph:
                        # switch to load_graphs(test_dataset.graphFile)
                        train_graph, _ = load_graphs(os.path.join(self._data_dir, "train_graph"))
                    else:
                        train_graph, _ = load_graphs(os.path.join(self._data_dir, "graph"))

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
                else:
                    if input_name == 'graph':
                        if multi_graph:
                            test_graph, _ = load_graphs(os.path.join(self._data_dir, "test_graph"))

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
        train_h5, val_h5, test_h5 = self.load_h5_files()

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

        if test_dataset:
            try:
                test_h5 = self.load_dataset(test_dataset, learning_method)
            except FileNotFoundError:
                logging.error("Couldn't load test set. File '%s' does not exist.", test_dataset)

        if val_dataset:
            try:
                val_h5 = self.load_dataset(val_dataset, learning_method)
            except FileNotFoundError:
                logging.error("Couldn't load validation set. File '%s' does not exist.", val_dataset)

        return train_h5, val_h5, test_h5

    def load_dataset(self, h5_path: pathlib.Path, learning_method: str) -> h5py.File:
        if h5_path.exists():
            try:
                h5_file = h5py.File(h5_path, 'r')
            except OSError as e:
                raise RuntimeError("Failed to load dataset from path " + str(h5_path)) from e
            if learning_method != "vae":
                for output_name in self._output_names_:
                    if output_name not in h5_file:
                        logging.error("The HDF5 file '%s' has to contain the dataset '%s'", h5_path.absolute(), output_name)
                        sys.exit(1)
            return h5_file

        raise FileNotFoundError(f"Data loading failure. File '{h5_path.absolute()}' does not exist.")


    def load_vae_data(self, batch_size, shuffle=False, input_names=None):
        self._input_names_ = input_names or []
        train_h5, _, test_h5 = self.load_h5_files(learning_method="vae")

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

    def check_bias(self, dataset: TrainingDataset=None, test_dataset: Dataset=None, val_dataset: Dataset=None):
        _, _, test = self.load_h5_files(
            "",
            pathlib.Path(dataset.path),
            pathlib.Path(val_dataset.path) if val_dataset else None,
            pathlib.Path(test_dataset.path) if test_dataset else None
        )

        if test is not None:
            data, label = np.array(test["data"]), np.array(test["softmax_label"]).astype(int)

            # load model
            ctx = mx.gpu() if mx.context.num_gpus() else mx.cpu()
            with warnings.catch_warnings():
                warnings.simplefilter("ignore")
                net = gluon.nn.SymbolBlock.imports(
                    self._model_dir_ + '/newest-symbol.json',
                    ["data"],
                    self._model_dir_ + '/newest-0000.params',
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


    @classmethod
    def load_retraining_conf(cls) -> RetrainingConf:
        """load the retraining configuration from the file system.
        It provides the information about datasets, like id and path.
        """

        try:
            conf = json.load((pathlib.Path(__file__).parent / "conf" / "retraining.json").open(), object_hook=lambda d: SimpleNamespace(**d))
            # Clean files that are not h5 files
            conf.changes = [dataset for dataset in conf.changes if pathlib.Path(dataset.path).suffix == ".h5"]
            return conf
        except FileNotFoundError:
            logging.warning("Retraining configuration not found. Fallback to 'train.h5' for training, 'test.h5' for testing and 'validation.h5' for validating.")
            path = "src/test/resources/training_data/GNN/CoraDgl/"

            if os.path.exists(path + "validation.h5"):
                return RetrainingConf(
                    testing=Dataset(id="test", path=path + "test.h5"),
                    validating=Dataset(id="validation", path=path + "validation.h5"),
                    changes=[TrainingDataset(id="train", path=path + "train.h5", retraining=True)]
                )
            else:
                return RetrainingConf(
                    testing=Dataset(id="test", path=path + "test.h5"),
                    validating=None,
                    changes=[TrainingDataset(id="train", path=path + "train.h5", retraining=True)]
                )