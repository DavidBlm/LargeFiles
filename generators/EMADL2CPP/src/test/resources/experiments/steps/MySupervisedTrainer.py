import os
import errno
import shutil
from Utils import *
import torch
import torch.nn as nn
import torch.nn.functional as F

class MySupervisedTrainer():

    def __init__(self, networkImplementation, trainData, schema_api, model_dir, model_prefix ="model"):
        self._network_ = networkImplementation
        self._schemaApi_ = schema_api
        self._train_loader_ = trainData
        self._model_dir_ = model_dir
        self._model_prefix_ = model_prefix

    def execute(self):
        criterion = translate_loss_name(self._schemaApi_.get_loss_value())
        optimizer = translate_optim_name(list(self._network_.parameters()), self._schemaApi_.get_optimizer_value(),
                                         self._schemaApi_.get_optimizer_learning_rate())
        begin_epoch= 0
        self._network_.train()
        num_epoch = self._schemaApi_.get_num_epoch()
        if os.path.isdir(self._model_dir_):
            shutil.rmtree(self._model_dir_)
        try:
            os.makedirs(self._model_dir_)
        except OSError:
            if not os.path.isdir(self._model_dir_):
                raise

        epoch_loss = 0
        epoch_accuracy = 0

        for epoch in range(num_epoch):  
            train_losses = []
            train_accuracy = []
            for i, data in enumerate(self._train_loader_, 0):
                # get the inputs; data is a list of [inputs, labels]
                inputs, labels = data
                # zero the parameter gradients
                optimizer.zero_grad()
                # forward + backward + optimize
                outputs = self._network_(inputs)
                
                loss = criterion(outputs, labels)
                train_losses.append(loss)
                
                _, predicted = torch.max(outputs.data, 1)
                train_accuracy.append(torch.tensor((torch.sum(predicted == labels).item() / len(predicted))))
                loss.backward()
                optimizer.step()
            epoch_loss = torch.stack(train_losses).mean().item()
            epoch_accuracy = torch.stack(train_accuracy).mean().item()
            print(f'Epoch:{epoch+1} Train Loss:{epoch_loss:.4f} Train Accuracy:{100*epoch_accuracy:.2f}%')

        print(f'Training Loss and accuracy after  {num_epoch} epochs,  Loss:{epoch_loss:.4f} , Accuracy:{100 *epoch_accuracy:.2f}%')
        #Saving model according to number of epoch
        torch.save({
            'epoch': str(num_epoch + begin_epoch),
            'model_state_dict': self._network_.state_dict(),
            'optimizer_state_dict': optimizer.state_dict(),
            'loss': epoch_loss}, self._model_dir_ + (self._model_prefix_ + "_" + str(num_epoch + begin_epoch)+ ".pth"))
        #Saving model in .pt format for prediction in c++
        model_scripted = torch.jit.script(self._network_) # Export to TorchScript
        network_path = self._model_dir_ + 'model_cpp.pt'
        model_scripted.save(self._model_dir_+'model_cpp.pt')
        return network_path , epoch_accuracy, epoch_loss