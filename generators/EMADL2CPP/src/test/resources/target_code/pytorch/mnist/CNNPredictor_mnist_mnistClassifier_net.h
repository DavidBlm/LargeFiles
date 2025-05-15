/* (c) https://github.com/MontiCore/monticore */
#ifndef CNNPREDICTOR_MNIST_MNISTCLASSIFIER_NET_0
#define CNNPREDICTOR_MNIST_MNISTCLASSIFIER_NET_0
#include <cassert>
#include <string>
#include <vector>
#include <torch/script.h>
#include <torch/torch.h>

class CNNPredictor_mnist_mnistClassifier_net_0{
    public:
    const std::string pt_file = "./model/mnist.LeNetNetwork/model_cpp.pt";
    torch::jit::script::Module module;

    explicit CNNPredictor_mnist_mnistClassifier_net_0(){
        init(pt_file);
    }

    void init(const std::string &pt_file){

        try {
            // Deserialize the ScriptModule from a file using torch::jit::load().
            module = torch::jit::load(pt_file);
        }
        catch (const c10::Error& e) {
            throw std::runtime_error("Error loading the model\n");
        }
        std::cout << "Model loaded\n";
    }

    void predict(const std::vector<float> &image_,
                            std::vector<float> &predictions_){

        at::Tensor inputTensors = torch::from_blob((float*)(image_.data()), {1,1,28,28}, at::TensorOptions());
        inputTensors = inputTensors.toType(at::kFloat);

        at::Tensor output = module.forward({inputTensors}).toTensor();
        std::vector<float> output_vector(output.data_ptr<float>(), output.data_ptr<float>() + output.numel());
        predictions_ = output_vector;
    }
};
#endif // CNNPREDICTOR_MNIST_MNISTCLASSIFIER_NET