import torch
import json

from tracking.RunTracker import MultiBackendTracker

class MyEvaluations():

    def __init__(self, trained_model_path, testData, schema_api, model_dir, cli_arguments):
        self._trained_model_ = torch.jit.load(trained_model_path)
        self._test_loader_ = testData
        self._model_dir = model_dir
        self._trained_model_path = trained_model_path
        self.tracker = MultiBackendTracker(cli_arguments)

    def save_results(self, accuracy):
        content = {
            'accuracy': accuracy
        }
        results_path = self._model_dir + "results.json"
        json_object = json.dumps(content, indent=4)
        with open(results_path, "w") as outfile:
            outfile.write(json_object)

    def execute(self):
        test_accuracies = []
        self._trained_model_.eval()
        # since we're not training, we don't need to calculate the gradients for our outputs
        with torch.no_grad():
            for data in self._test_loader_:
                inputs, labels = data
                outputs = self._trained_model_(inputs)
                _, predicted = torch.max(outputs.data, 1)

                test_accuracies.append(torch.tensor((torch.sum(predicted == labels).item() / len(predicted))))

            accuracy_mean = torch.stack(test_accuracies).mean().item()
            accuracy = round(accuracy_mean * 100, 2)
            self.tracker.log_metric("test_accuracy", accuracy_mean)
            print(f'Accuracy: {accuracy}%')
            self.save_results(accuracy)

        return accuracy
