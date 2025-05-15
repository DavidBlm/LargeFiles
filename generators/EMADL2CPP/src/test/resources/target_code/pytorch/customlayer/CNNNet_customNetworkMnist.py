import torch
import torch.nn as nn
import torch.nn.functional as F
from dgl.nn import GraphConv
sys.path.insert(1, 'src/test/resources/custom_files/python/pytorch')
from custom_layers import *

class CNNNet_customNetworkMnist(nn.Module):
    def __init__(self):
        super(CNNNet_customNetworkMnist, self).__init__()

        self.conv1_ = nn.Conv2d(in_channels=1, out_channels=20, kernel_size=(5,5), stride=(1,1), bias=True)
        # conv1_, output shape: {[20,24,24]}
        self.pool1_ = nn.MaxPool2d(kernel_size=2, stride=2)
        # pool1_, output shape: {[20,12,12]}

        self.conv2_ = nn.Conv2d(in_channels=20, out_channels=50, kernel_size=(5,5), stride=(1,1), bias=True)
        # conv2_, output shape: {[50,8,8]}
        self.pool2_ = nn.MaxPool2d(kernel_size=2, stride=2)
        # pool2_, output shape: {[50,4,4]}

        self.fc2_ = nn.Linear(in_features=50 * 4 * 4, out_features=500,bias=True)
        # fc2_, output shape: {[500,1,1]}
        self.myelu2_ = MyELU.MyELU(alpha=1)
        # myelu2_, output shape: {[500,1,1]}


        self.fc3_ = nn.Linear(in_features=500, out_features=10,bias=True)
        # fc3_, output shape: {[10,1,1]}

        self.predictions_ = nn.Identity()

        pass

    def forward(self , image_ ):

        conv1_ = self.conv1_(image_)
        pool1_ = self.pool1_(conv1_)

        conv2_ = self.conv2_(pool1_)
        pool2_ = self.pool2_(conv2_)

        fc2_ = pool2_.reshape(pool2_.shape[0], -1)
        fc2_ = self.fc2_(fc2_)

        myelu2_ = self.myelu2_(fc2_)

        fc3_ = self.fc3_(myelu2_)
        softmax3_  = F.log_softmax(fc3_, dim=-1)

        predictions_ = self.predictions_(softmax3_)

        return predictions_


