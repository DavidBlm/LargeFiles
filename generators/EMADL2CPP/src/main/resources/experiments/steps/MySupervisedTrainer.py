import os
import shutil
from tracking.RunTracker import MultiBackendTracker

from Utils import *


class MySupervisedTrainer():

    def __init__(self, networkImplementation, trainData, schema_api, model_dir, cli_arguments, model_prefix="model"):
        self._network_ = networkImplementation
        self._schemaApi_ = schema_api
        self._train_loader_ = trainData
        self._model_dir_ = model_dir
        self.model_prefix = model_prefix
        self.optimizer_func_dict = {
            "weight_decay": self._schemaApi_.get_optimizer_weight_decay,
            "sgd_momentum": self._schemaApi_.get_optimizer_sgd_momentum,
            "adam_beta1": self._schemaApi_.get_optimizer_adam_beta1,
            "adam_beta2": self._schemaApi_.get_optimizer_adam_beta2,
            "adam_epsilon": self._schemaApi_.get_optimizer_adam_epsilon
        }
        self.optimizer_default_dict = {
            "weight_decay": 0,
            "sgd_momentum": 0,
            "adam_beta": (0.9, 0.999),
            "adam_epsilon": 1e-8
        }
        self.tracker = MultiBackendTracker(cli_arguments)

    def get_optimizer_parameter(self, name):
        try:
            if (name == "adam_beta"):
                return (self.optimizer_func_dict["adam_beta1"](), self.optimizer_func_dict["adam_beta2"]())
            return self.optimizer_func_dict[name]()
        except:
            return self.optimizer_default_dict[name]

    def execute(self):
        criterion = translate_loss_name(self._schemaApi_.get_loss_value())
        optimizer = translate_optim_name(list(self._network_.parameters()),
                                         self._schemaApi_.get_optimizer_value(),
                                         self._schemaApi_.get_optimizer_learning_rate(),
                                         self.get_optimizer_parameter("weight_decay"),
                                         self.get_optimizer_parameter("sgd_momentum"),
                                         self.get_optimizer_parameter("adam_beta"),
                                         self.get_optimizer_parameter("adam_epsilon"))

        begin_epoch = 0
        self._network_.train()
        num_epoch = self._schemaApi_.get_num_epoch()
        if os.path.isdir(self._model_dir_):
            shutil.rmtree(self._model_dir_)
        try:
            os.makedirs(self._model_dir_)
        except OSError:
            if not os.path.isdir(self._model_dir_):
                raise

        self.evaluate_start_network(optimizer)

        epoch_loss = 0
        epoch_accuracy = 0

        for epoch in range(num_epoch):
            train_losses = []
            train_accuracy = []
            for i, data in enumerate(self._train_loader_, 0):
                inputs, labels = data
                optimizer.zero_grad()
                outputs = self._network_(inputs)

                loss = criterion(outputs, labels)
                train_losses.append(loss)

                _, predicted = torch.max(outputs.data, 1)
                train_accuracy.append(torch.tensor((torch.sum(predicted == labels).item() / len(predicted))))
                loss.backward()
                optimizer.step()
            epoch_loss = torch.stack(train_losses).mean().item()
            epoch_accuracy = torch.stack(train_accuracy).mean().item()
            self.tracker.log_metric("train_loss", epoch_loss, step=epoch)
            self.tracker.log_metric("train_accuracy", epoch_accuracy, step=epoch)
            print(f'Epoch:{epoch + 1} Train Loss:{epoch_loss:.4f} Train Accuracy:{100 * epoch_accuracy:.2f}%')

        print(
            f'Training Loss and accuracy after  {num_epoch} epochs,  Loss:{epoch_loss:.4f} , Accuracy:{100 * epoch_accuracy:.2f}%')

        # Saving model in .pt format for prediction in c++
        model_scripted = torch.jit.script(self._network_)  # Export to TorchScript
        network_path = self._model_dir_ + 'model_cpp.pt'
        model_scripted.save(self._model_dir_ + 'model_cpp.pt')
        return network_path, epoch_accuracy, epoch_loss

    def evaluate_start_network(self, optimizer):
        train_accuracy = []
        for i, data in enumerate(self._train_loader_, 0):
            inputs, labels = data
            optimizer.zero_grad()
            outputs = self._network_(inputs)

            _, predicted = torch.max(outputs.data, 1)
            train_accuracy.append(torch.tensor((torch.sum(predicted == labels).item() / len(predicted))))
        epoch_accuracy = torch.stack(train_accuracy).mean().item()
        print(f'Epoch:0 Train Accuracy:{100 * epoch_accuracy:.2f}%')
