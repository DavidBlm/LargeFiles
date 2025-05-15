# (c) https://github.com/MontiCore/monticore  
import logging
import os
import h5py
import sys
import numpy as np

class CNNDataLoader_mnist_mnistClassifier_net:
    
    def __init__(self, data_dir, input_names, output_names, output_shapes):
        
        self._data_dir_ = data_dir
        self._input_names_ = input_names
        self._output_names_ = output_names
        self._output_shapes_ = output_shapes

    def load_data_generators(self, batch_size, normalize):
        train_h5, test_h5 = self.load_h5_files()

        train_generator = self._generator(train_h5, batch_size)
        test_generator = self._generator(test_h5, batch_size)

        if normalize:
            data_mean, data_std = self._get_mean_std(train_h5)
        else:
            data_mean, data_std = 0,1

        data_size = train_h5[self._input_names_[0]].shape[0]
        steps_per_epoch = data_size//batch_size
	
        data_size = test_h5[self._input_names_[0]].shape[0]
        validation_steps  = data_size//batch_size

        if validation_steps == 0 and data_size > 0:
            validation_steps = data_size
        elif data_size == 0:
            raise Exception("Test data set is empty. \n")
            
        return train_generator, test_generator, data_mean, data_std, steps_per_epoch, validation_steps 

    
    def _generator(self, h5_container, batch_size):
        #all tables in the file have to have same number of samples
        data_size = h5_container[self._input_names_[0]].shape[0]

        for shape in self._output_shapes_:
            if len(shape) == 3:
                logging.warning("Outputs with 3 dimensions will be interpreted as having a channel diemnsion. Outputs, as inputs are expecte to be passed in channel first format, but internally internally tensorlow needs channel last format, therfore there will be a permutation of the channel of the learned / predicted target to channel first.\n\n")
                break

        while(True):
            for i in range(0, data_size, batch_size):

                if i+batch_size < data_size:
                    offset = batch_size
                else:
                    offset = data_size - i - 1

                data_batch = [h5_container[key][i:i+offset].astype(np.float32) for key in self._input_names_]     
                    
                target_batch = [h5_container[key][i:i+offset].astype(np.float32) for key in self._output_names_]

                yield (data_batch, target_batch)
    
    
    def _get_mean_std(self, h5_container):
            
        input_means = []
        input_stds = []

        if "statistics" in h5_container.keys():
            logging.info("Statistics found.")
            data = h5_container["statistics"]

            input_means = {key: data[key+"_mean"] for key in self._input_names_}
            input_stds = {key: data[key+"_std"] for key in self._input_names_}

        else:
            logging.info("Statistics NOT found.")
            logging.info("Calculating statistics...")

            data_size = h5_container[self._input_names_[0]].shape[0]

            logging.info("Calculating statistics for " + str(len(self._input_names_)) + " inputs, " + str(len(self._output_names_)) + " outputs, " + str(data_size) + " data points...")

            batch_size = 100

            #Calculating means...
            logging.info("Calculating means... ")

            input_means_running = {key: 0. for key in self._input_names_}
                
            for i in range(0, data_size, batch_size):

                if i+batch_size < data_size:
                    offset = batch_size
                else:
                    offset = data_size - i - 1

                for key in self._input_names_:
                    input_means_running[key] += np.mean(h5_container[key][i:i+offset].astype(np.float32))*(offset/float(batch_size))

            input_means = {key: (input_means_running[key] / (data_size//batch_size + offset/float(batch_size))) for key in self._input_names_}

            logging.info("Done calculating means!")
            logging.info("Input means: " + str(input_means) + "\n\n")


            #Calculating stds...
            logging.info("Calculating stds...")

            input_stds_running = {key: 0. for key in self._input_names_}

            for i in range(0, data_size, batch_size):

                if i+batch_size < data_size:
                    offset = batch_size
                else:
                    offset = data_size - i - 1

                for key in self._input_names_:
                    input_stds_running[key] += np.mean((h5_container[key][i:i+offset].astype(np.float32) - input_means[key])**2)*(offset/float(batch_size))

            std_eps = 1e-08
            input_stds = {key: np.sqrt(input_stds_running[key] / (data_size//batch_size + offset/float(batch_size)) + std_eps) for key in self._input_names_}

            logging.info("Done calculating stds!")
            logging.info("Input stds: " + str(input_stds) + "\n\n")

        return input_means, input_stds
    
    
    def load_h5_files(self):
        train_h5 = None
        test_h5 = None
        train_path = self._data_dir_ + "train.h5"
        test_path = self._data_dir_ + "test.h5"
        if os.path.isfile(train_path):
            train_h5 = h5py.File(train_path, 'r')
            if not (self._input_names_[0] in train_h5 and self._output_names_[0] in train_h5):
                logging.error("The HDF5 file '" + os.path.abspath(train_path) + "' has to contain the datasets: "
                              + "'" + str(self._input_names_) + "', '" + str(self._output_names_) + "'")
                sys.exit(1)
					  
            if os.path.isfile(test_path):
                test_h5 = h5py.File(test_path, 'r')
                if not (self._input_names_[0] in test_h5 and self._output_names_[0] in test_h5):
                    logging.error("The HDF5 file '" + os.path.abspath(test_path) + "' has to contain the datasets: "
                                  + "'" + str(self._input_names_) + "', '" + str(self._output_names_) + "'")
                    sys.exit(1)
            else:
                logging.warning("Couldn't load test set. File '" + os.path.abspath(test_path) + "' does not exist.")
            return train_h5, test_h5
        else:
            logging.error("Data loading failure. File '" + os.path.abspath(train_path) + "' does not exist.")
            sys.exit(1)
