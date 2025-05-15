import torch
import torch.nn as nn
import torch.nn.functional as F

class MyEvaluation():

    def __init__(self, trained_model_path, testData, schema_api, model_dir):
        self._trained_model_ = torch.jit.load(trained_model_path)
        self._test_loader_ = testData

    def execute(self):
        test_accuracy = []
        self._trained_model_.eval()
        # since we're not training, we don't need to calculate the gradients for our outputs
        with torch.no_grad():
            for data in self._test_loader_:
                inputs, labels = data
                # calculate outputs by running images through the network
                outputs = self._trained_model_(inputs)
                # the class with the highest energy is what we choose as prediction
                _, predicted = torch.max(outputs.data, 1)
                test_accuracy.append(torch.tensor((torch.sum(predicted == labels).item() / len(predicted))))
            print(f'Accuracy:{(100 * (torch.tensor(test_accuracy)).mean().item()):.2f}%')
            accuracy = torch.stack(test_accuracy).mean().item()
        return accuracy
