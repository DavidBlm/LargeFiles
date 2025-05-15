# (c) https://github.com/MontiCore/monticore
import importlib
import json
import logging
import os
import pathlib
import sys
import typing as t
from types import SimpleNamespace

import h5py
import mxnet as mx
import numpy as np
from mxnet import nd


from CNNDatasets_mountaincar_master_actor import Dataset, TrainingDataset, RetrainingConf

class CNNDataLoader_mountaincar_master_actor: # pylint: disable=invalid-name
    _input_names_ = ['state']
    _output_names_ = ['action_label']

    def __init__(self):
        self._data_dir = pathlib.Path("data/")

    def load_data(self, batch_size, shuffle=False, multi_graph=False, dataset: TrainingDataset=None, test_dataset: Dataset=None):
        if not dataset: 
            raise KeyError("No dataset specified.")

        train_h5, test_h5 = self.load_h5_files(
            "", 
            pathlib.Path(dataset.path), 
            pathlib.Path(test_dataset.path) if test_dataset else None
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
        return train_iter, test_iter, data_mean, data_std, train_images, test_images, train_graph, test_graph

    def load_preprocessed_data(self, batch_size, preproc_lib, shuffle=False):
        train_h5, test_h5 = self.load_h5_files()

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

    def load_h5_files(self, learning_method="", dataset: pathlib.Path = None, test_dataset: t.Optional[str] = None):
        if not dataset:
            dataset = self._data_dir / "train.h5"
        train_h5 = self.load_dataset(dataset, learning_method)
        test_h5 = None
        
        if test_dataset:
            try: 
                test_h5 = self.load_dataset(test_dataset, learning_method)
            except FileNotFoundError: 
                logging.error("Couldn't load test set. File '%s' does not exist.", test_dataset)
            
        return train_h5, test_h5

    def load_dataset(self, h5_path: pathlib.Path, learning_method: str) -> h5py.File:
        if h5_path.exists():
            try:
                h5_file = h5py.File(h5_path, 'r')
            except OSError as e:
                raise RuntimeError("Failed to load dataset from path " + str(h5_path)) from e
            for input_name in self._input_names_:
                if input_name not in h5_file:
                    logging.error("The HDF5 file '%s' has to contain the dataset '%s'", h5_path.absolute(), input_name)
                    sys.exit(1)
            if learning_method != "vae":
                for output_name in self._output_names_:
                    if output_name not in h5_file:
                        logging.error("The HDF5 file '%s' has to contain the dataset '%s'", h5_path.absolute(), output_name)
                        sys.exit(1)
            return h5_file

        raise FileNotFoundError(f"Data loading failure. File '{h5_path.absolute()}' does not exist.")


    def load_vae_data(self, batch_size, shuffle=False, input_names=None):
        self._input_names_ = input_names or []
        train_h5, test_h5 = self.load_h5_files(learning_method="vae")

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
            logging.warning("Retraining configuration not found. Fallback to 'train.h5' for training and 'test.h5' for testing.")
            path = "data/"
            return RetrainingConf(
                testing=Dataset(id="test", path=path + "test.h5"),
                changes=[TrainingDataset(id="train", path=path + "train.h5", retraining=True)]
            )