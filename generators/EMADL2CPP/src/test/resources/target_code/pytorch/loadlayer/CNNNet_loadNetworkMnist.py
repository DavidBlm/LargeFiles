import torch
import torch.nn as nn
import torch.nn.functional as F
from dgl.nn import GraphConv

class CNNNet_loadNetworkMnist(nn.Module):
    def __init__(self):
        super(CNNNet_loadNetworkMnist, self).__init__()
        trainedFile = None
        for file in os.listdir("src/test/resources/pretrained/pytorch"):
            if "model_cpp" in file and ".pt" in file:
                trainedFile = file

            inputNames = ["data"]
            zeroInputs = [torch.zeros((1,1,28,28))]
        if trainedFile:
            self.loadnetwork1_ = torch.load("src/test/resources/pretrained/pytorch/" + trainedFile)
        else:
            raise FileNotFoundError("Model files were not found in 'src/test/resources/pretrained/pytorch'.")

        outputSize=1
        for x in (1,10,):
            outputSize = outputSize * x
        self.loadnetwork1_fc_ = nn.LazyLinear(out_features=outputSize, bias=False)


        self.fc1_ = nn.Linear(in_features=1 * 10 * 1, out_features=40,bias=True)
        # fc1_, output shape: {[40,1,1]}

        self.fc2_ = nn.Linear(in_features=40, out_features=10,bias=True)
        # fc2_, output shape: {[10,1,1]}

        self.predictions_ = nn.Identity()

        pass

    def forward(self , image_ ):
        loadnetwork1_ = self.loadnetwork1_(image_)
        loadnetwork1_ = self.loadnetwork1_fc_(loadnetwork1_)

        fc1_ = loadnetwork1_.reshape(loadnetwork1_.shape[0], -1)
        fc1_ = self.fc1_(fc1_)

        fc2_ = self.fc2_(fc1_)
        softmax2_  = F.log_softmax(fc2_, dim=-1)

        predictions_ = self.predictions_(softmax2_)

        return predictions_


