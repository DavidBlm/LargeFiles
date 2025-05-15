# (c) https://github.com/MontiCore/monticore
from caffe2.python import workspace, core, utils, model_helper, brew, optimizer
from caffe2.python.predictor import mobile_exporter
from caffe2.proto import caffe2_pb2
import numpy as np
import h5py
import math
import datetime
import logging
import os, gc
import sys
import lmdb

class CNNCreator_mnist_mnistClassifier_net:

    def __init__(self, data_cleaner):
        self.module = None
        self._current_dir_ = os.path.join('./')
        self._data_dir_    = os.path.join(self._current_dir_, 'data/mnist.LeNetNetwork')
        self._model_dir_   = os.path.join(self._current_dir_, 'model', 'mnist.LeNetNetwork')
        self._data_cleaner_ = data_cleaner

        self._init_net_    = os.path.join(self._model_dir_, 'init_net.pb')
        self._predict_net_ = os.path.join(self._model_dir_, 'predict_net.pb')

    def get_total_num_iter(self, num_epoch, batch_size, dataset_size):
        #Force floating point calculation
        batch_size_float = float(batch_size)
        dataset_size_float = float(dataset_size)

        iterations_float = math.ceil(num_epoch*(dataset_size_float/batch_size_float))
        iterations_int = int(iterations_float)

        return iterations_int

    def get_epoch_as_iter(self, num_epoch, batch_size, dataset_size):   #To print metric durint training process
        #Force floating point calculation
        batch_size_float = float(batch_size)
        dataset_size_float = float(dataset_size)

        epoch_float = math.ceil(dataset_size_float/batch_size_float)
        epoch_int = int(epoch_float)

        return epoch_int

    def load_h5(self, _data_dir, type):
        '''given data_dir, load data and label as np.array'''
        data = h5py.File(os.path.join(_data_dir, type+'.h5'), "r")
        return np.array(data["data"]), np.array(data["softmax_label"]).astype(np.int32)

    # function for creating lmdb files
    def create_db(self, data, label, path, db_type):

        # check if olf data.minidb exist in path and remove if True
        if os.path.exists(os.path.join(path, 'data.minidb')):
            os.remove(os.path.join(path, 'data.minidb'))

        # start create new data.minidb
        db = core.C.create_db(db_type, os.path.join(path, 'data.minidb'), core.C.Mode.write)
        transaction = db.new_transaction()

        label = label.astype(np.int32)
        for i in range(data.shape[0]):
            feature_and_label = caffe2_pb2.TensorProtos()
            feature_and_label.protos.extend([
                utils.NumpyArrayToCaffe2Tensor(data[i]),
                utils.NumpyArrayToCaffe2Tensor(label[i])
            ])
            transaction.put(
                '%03d'.format(i),
                feature_and_label.SerializeToString()
            )        
        # Close the transaction, and then close the db.
        del transaction
        del db

    
    def add_input(self, model, cleaning, cleaning_params, data_imbalance, data_imbalance_params, data_type, batch_size, db, db_type, device_opts):
        with core.DeviceScope(device_opts):
            if not os.path.isdir(db):
                logging.error("Data loading failure. Directory '" + os.path.abspath(db) + "' does not exist.")
                # mkdir for db path
                os.mkdir(db)
                logging.info("Create path: '" + os.path.abspath(db) + "'")
            elif not os.path.isdir(self._data_dir_):
                logging.error("Data loading failure. Directory '" + os.path.abspath(self._data_dir_) + "' does not exist.")
                sys.exit(1)

            # load h5 data and label
            data_, label_ = self.load_h5(self._data_dir_, data_type)       
            
            if cleaning == 'remove':
                # start cleaning 
                data_, label_ = self._data_cleaner_.clean_data(data_, label_, cleaning, cleaning_params) 
                
                if data_imbalance == 'image_augmentation':
                    # use data augmentation for upsampling the imbalanced dataset
                    data_, label_ = self._data_cleaner_.image_augmentation(data_, label_, data_imbalance, data_imbalance_params) 

            dataset_size = data_.shape[0]
            print('dataset_size:', dataset_size)

            # create minidb 
            self.create_db(data_, label_, db, db_type)

            if not os.path.isfile(os.path.join(db, 'data.minidb')):
                logging.error("Data loading failure. Directory '" + os.path.join(db, 'data.minidb') + "' does not contain data.minidb file.")
                sys.exit(1)

            # load the data
            data, label = brew.db_input(
                model,
                blobs_out=["data", "label"],
                batch_size=batch_size,
                db=os.path.join(db, 'data.minidb'),
                db_type=db_type,
            )
            return data, label, dataset_size

    def create_model(self, model, data, device_opts, is_test):
        with core.DeviceScope(device_opts):

            image_ = data
            # image_, output shape: {[1,28,28]}
            conv1_ = brew.conv(model, image_, 'conv1_', dim_in=1, dim_out=20, kernel=5, stride=1, pad=1)
            # conv1_, output shape: {[20,28,28]}
            pool1_ = brew.max_pool(model, conv1_, 'pool1_', kernel=2, stride=2, pad=1)
            # pool1_, output shape: {[20,14,14]}
            conv2_ = brew.conv(model, pool1_, 'conv2_', dim_in=20, dim_out=50, kernel=5, stride=1, pad=1)
            # conv2_, output shape: {[50,14,14]}
            pool2_ = brew.max_pool(model, conv2_, 'pool2_', kernel=2, stride=2, pad=1)
            # pool2_, output shape: {[50,7,7]}
            fc2_ = brew.fc(model, pool2_, 'fc2_', dim_in=50 * 7 * 7, dim_out=500)
            # fc2_, output shape: {[500,1,1]}
            relu2_ = brew.relu(model, fc2_, fc2_)
            fc3_ = brew.fc(model, relu2_, 'fc3_', dim_in=500, dim_out=10)
            # fc3_, output shape: {[10,1,1]}
            predictions_ = brew.softmax(model, fc3_, 'predictions_')

            return predictions_


    # this adds the loss and optimizer
    def add_training_operators(self, model, output, label, device_opts, loss, opt_type, base_learning_rate, policy, stepsize, epsilon, beta1, beta2, gamma, momentum) :
        with core.DeviceScope(device_opts):
            if loss == 'cross_entropy':
                xent = model.LabelCrossEntropy([output, label], 'xent')
                loss = model.AveragedLoss(xent, "loss")
            elif loss == 'euclidean':
                dist = model.net.SquaredL2Distance([label, output], 'dist')
                loss = dist.AveragedLoss([], ['loss'])

            model.AddGradientOperators([loss])

            if opt_type == 'adam':
                if policy == 'step':
                    opt = optimizer.build_adam(model, base_learning_rate=base_learning_rate, policy=policy, stepsize=stepsize, beta1=beta1, beta2=beta2, epsilon=epsilon)
                elif policy == 'fixed' or policy == 'inv':
                    opt = optimizer.build_adam(model, base_learning_rate=base_learning_rate, policy=policy, beta1=beta1, beta2=beta2, epsilon=epsilon)
                print("adam optimizer selected")
            elif opt_type == 'sgd':
                if policy == 'step':
                    opt = optimizer.build_sgd(model, base_learning_rate=base_learning_rate, policy=policy, stepsize=stepsize, gamma=gamma, momentum=momentum)
                elif policy == 'fixed' or policy == 'inv':
                    opt = optimizer.build_sgd(model, base_learning_rate=base_learning_rate, policy=policy, gamma=gamma, momentum=momentum)
                print("sgd optimizer selected")
            elif opt_type == 'rmsprop':
                if policy == 'step':
                    opt = optimizer.build_rms_prop(model, base_learning_rate=base_learning_rate, policy=policy, stepsize=stepsize, decay=gamma, momentum=momentum, epsilon=epsilon)
                elif policy == 'fixed' or policy == 'inv':
                    opt = optimizer.build_rms_prop(model, base_learning_rate=base_learning_rate, policy=policy, decay=gamma, momentum=momentum, epsilon=epsilon)
                print("rmsprop optimizer selected")
            elif opt_type == 'adagrad':
                if policy == 'step':
                    opt = optimizer.build_adagrad(model, base_learning_rate=base_learning_rate, policy=policy, stepsize=stepsize, decay=gamma, epsilon=epsilon)
                elif policy == 'fixed' or policy == 'inv':
                    opt = optimizer.build_adagrad(model, base_learning_rate=base_learning_rate, policy=policy, decay=gamma, epsilon=epsilon)
                print("adagrad optimizer selected")

    def add_accuracy(self, model, output, label, device_opts, eval_metric):
        with core.DeviceScope(device_opts):
            if eval_metric == 'accuracy':
                accuracy = brew.accuracy(model, [output, label], "accuracy")
            elif eval_metric == 'top_k_accuracy':
                accuracy = brew.accuracy(model, [output, label], "accuracy", top_k=3)
            return accuracy

    def train(  self, num_epoch=1000, 
                batch_size=64, 
                context='gpu', 
                eval_metric='accuracy', 
                loss='cross_entropy', 
                opt_type='adam', 
                base_learning_rate=0.001, 
                weight_decay=0.001, 
                policy='fixed', 
                stepsize=1, 
                epsilon=1E-8, 
                beta1=0.9, 
                beta2=0.999, 
                gamma=0.999, 
                momentum=0.9,
                cleaning=None, 
                cleaning_params=(None),
                data_imbalance=None,
                data_imbalance_params=(None)) :

        if context == 'cpu':
            device_opts = core.DeviceOption(caffe2_pb2.CPU, 0)
            print("CPU mode selected")
        elif context == 'gpu':
            device_opts = core.DeviceOption(caffe2_pb2.CUDA, 0)
            print("GPU mode selected")

        workspace.ResetWorkspace(self._model_dir_)

        arg_scope = {"order": "NCHW"}

        # ------- Training model -------

        train_model= model_helper.ModelHelper(name="train_net", arg_scope=arg_scope)
        
        data, label, train_dataset_size = self.add_input(train_model, cleaning, cleaning_params, data_imbalance, data_imbalance_params, data_type='train', batch_size=batch_size, db=os.path.join(self._data_dir_, 'caffe2_db', 'train_lmdb'), db_type='minidb', device_opts=device_opts)
       
        predictions_ = self.create_model(train_model, data, device_opts=device_opts, is_test=False)
        self.add_training_operators(train_model, predictions_, label, device_opts, loss, opt_type, base_learning_rate, policy, stepsize, epsilon, beta1, beta2, gamma, momentum)
        
        if not loss == 'euclidean':
            self.add_accuracy(train_model, predictions_, label, device_opts, eval_metric)
        with core.DeviceScope(device_opts):
            brew.add_weight_decay(train_model, weight_decay)

        # Initialize and create the training network
        workspace.RunNetOnce(train_model.param_init_net)
        workspace.CreateNet(train_model.net, overwrite=True)

        # Main Training Loop
        iterations = self.get_total_num_iter(num_epoch, batch_size, train_dataset_size)
        epoch_as_iter = self.get_epoch_as_iter(num_epoch, batch_size, train_dataset_size)
        
        print("\n------- Starting Training -------")
        
        start_date = datetime.datetime.now()
        for i in range(iterations):
            workspace.RunNet(train_model.net)
            
            if i % 50 == 0 or i % epoch_as_iter == 0:
                if not loss == 'euclidean':
                    print('Iter ' + str(i) + ': ' + 'Loss ' + str(workspace.FetchBlob("loss")) + ' - ' + 'Accuracy ' + str(workspace.FetchBlob('accuracy')))
                else:
                    print('Iter ' + str(i) + ': ' + 'Loss ' + str(workspace.FetchBlob("loss")))

                current_time = datetime.datetime.now()
                elapsed_time = current_time - start_date
                print('Progress: ' + str(i) + '/' + str(iterations) + ', ' +'Current time spent: ' + str(elapsed_time))
                
        current_time = datetime.datetime.now()
        elapsed_time = current_time - start_date

        print('Progress: ' + str(iterations) + '/' + str(iterations) + '\tTraining done: Total time spent: ' + str(elapsed_time))

        # ------- Testing model. -------
        
        print("\n------- Running Test model -------")
        
        test_model= model_helper.ModelHelper(name="test_net", arg_scope=arg_scope, init_params=False)
        data, label, test_dataset_size = self.add_input(test_model, cleaning, cleaning_params, data_imbalance, data_imbalance_params, data_type='test', batch_size=batch_size, db=os.path.join(self._data_dir_, 'caffe2_db', 'test_lmdb'), db_type='minidb', device_opts=device_opts)
        predictions_ = self.create_model(test_model, data, device_opts=device_opts, is_test=True)
        
        if not loss == 'euclidean':
            self.add_accuracy(test_model, predictions_, label, device_opts, eval_metric)
        workspace.RunNetOnce(test_model.param_init_net)
        workspace.CreateNet(test_model.net, overwrite=True)

        # Main Testing Loop
        print('test_dataset_size=', test_dataset_size, 'batch_size=', batch_size)
        test_accuracy = np.zeros(int(test_dataset_size/batch_size))
        start_date = datetime.datetime.now()
        
        for i in range(int(test_dataset_size/batch_size)):
            # Run a forward pass of the net on the current batch
            workspace.RunNet(test_model.net)
            
            # Collect the batch accuracy from the workspace
            if not loss == 'euclidean':
                test_accuracy[i] = workspace.FetchBlob('accuracy')
                print('Iter ' + str(i) + ': ' + 'Accuracy ' + str(workspace.FetchBlob("accuracy")))
            else:
                test_accuracy[i] = workspace.FetchBlob("loss")
                print('Iter ' + str(i) + ': ' + 'Loss ' + str(workspace.FetchBlob("loss")))

            current_time = datetime.datetime.now()
            elapsed_time = current_time - start_date
            print('Progress: ' + str(i) + '/' + str(test_dataset_size/batch_size) + ', ' +'Current time spent: ' + str(elapsed_time))
        
        current_time = datetime.datetime.now()
        elapsed_time = current_time - start_date
        print('Progress: ' + str(test_dataset_size/batch_size) + '/' + str(test_dataset_size/batch_size) + '\tTesting done: Total time spent: ' + str(elapsed_time))
        print('Test accuracy mean: {:.9f}'.format(test_accuracy.mean()))

        # ------- Deployment model. -------
        # We simply need the main AddModel part.
        deploy_model = model_helper.ModelHelper(name="deploy_net", arg_scope=arg_scope, init_params=False)
        self.create_model(deploy_model, "data", device_opts, is_test=True)

        print("\n------- Saving deploy model -------")
        self.save_net(self._init_net_, self._predict_net_, deploy_model)

        # ------- Check Bias. -------
        if data_imbalance_params['check_bias']:
            data, label = self.load_h5(self._data_dir_, 'test')
            self._data_cleaner_.check_bias(data, label, self._model_dir_, device_opts)

    def save_net(self, init_net_path, predict_net_path, model):

        init_net, predict_net = mobile_exporter.Export(
            workspace,
            model.net,
            model.params
        )

        try:
            os.makedirs(self._model_dir_)
        except OSError:
            if not os.path.isdir(self._model_dir_):
                raise

        print("Save the model to init_net.pb and predict_net.pb")
        
        with open(predict_net_path, 'wb') as f:
            f.write(model.net._net.SerializeToString())
        with open(init_net_path, 'wb') as f:
            f.write(init_net.SerializeToString())

        print("Save the model to init_net.pbtxt and predict_net.pbtxt as additional information")

        with open(init_net_path.replace('.pb','.pbtxt'), 'w') as f:
            f.write(str(init_net))
        with open(predict_net_path.replace('.pb','.pbtxt'), 'w') as f:
            f.write(str(predict_net))
        print("== Saved init_net and predict_net ==")


    def load_net(self, init_net_path, predict_net_path, device_opts):
        if not os.path.isfile(init_net_path):
            logging.error("Network loading failure. File '" + os.path.abspath(init_net_path) + "' does not exist.")
            sys.exit(1)
        elif not os.path.isfile(predict_net_path):
            logging.error("Network loading failure. File '" + os.path.abspath(predict_net_path) + "' does not exist.")
            sys.exit(1)

        init_def = caffe2_pb2.NetDef()
        with open(init_net_path, 'rb') as f:
            init_def.ParseFromString(f.read())
            init_def.device_option.CopyFrom(device_opts)
            workspace.RunNetOnce(init_def.SerializeToString())

        net_def = caffe2_pb2.NetDef()
        with open(predict_net_path, 'rb') as f:
            net_def.ParseFromString(f.read())
            net_def.device_option.CopyFrom(device_opts)
            workspace.CreateNet(net_def.SerializeToString(), overwrite=True)
        print("*** Loaded init_net and predict_net ***")
