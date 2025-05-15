import torch
import torch.nn as nn
import torch.nn.functional as F
from dgl.nn import GraphConv

class CNNNet_cifar10_cifar10Classifier_net(nn.Module):
    def __init__(self):
        super(CNNNet_cifar10_cifar10Classifier_net, self).__init__()

        self.conv2_1_ = nn.Conv2d(in_channels=3, out_channels=8, kernel_size=(3,3), stride=(1,1), bias=True,padding = 'same')
        # conv2_1_, output shape: {[8,32,32]}

        self.batchnorm2_1_ = nn.BatchNorm2d(num_features=8)
        # batchnorm2_1_, output shape: {[8,32,32]}

        self.relu2_1_ = nn.ReLU()

        self.conv3_1_ = nn.Conv2d(in_channels=8, out_channels=8, kernel_size=(3,3), stride=(1,1), bias=True,padding = 'same')
        # conv3_1_, output shape: {[8,32,32]}

        self.batchnorm3_1_ = nn.BatchNorm2d(num_features=8)
        # batchnorm3_1_, output shape: {[8,32,32]}

        self.conv2_2_ = nn.Conv2d(in_channels=3, out_channels=8, kernel_size=(1,1), stride=(1,1), bias=True,padding = 'same')
        # conv2_2_, output shape: {[8,32,32]}

        self.batchnorm2_2_ = nn.BatchNorm2d(num_features=8)
        # batchnorm2_2_, output shape: {[8,32,32]}


        self.relu4_ = nn.ReLU()

        self.conv5_1_ = nn.Conv2d(in_channels=8, out_channels=16, kernel_size=(3,3), stride=(2,2), bias=True,padding = 1)
        # conv5_1_, output shape: {[16,16,16]}

        self.batchnorm5_1_ = nn.BatchNorm2d(num_features=16)
        # batchnorm5_1_, output shape: {[16,16,16]}

        self.relu5_1_ = nn.ReLU()

        self.conv6_1_ = nn.Conv2d(in_channels=16, out_channels=16, kernel_size=(3,3), stride=(1,1), bias=True,padding = 'same')
        # conv6_1_, output shape: {[16,16,16]}

        self.batchnorm6_1_ = nn.BatchNorm2d(num_features=16)
        # batchnorm6_1_, output shape: {[16,16,16]}

        self.conv5_2_ = nn.Conv2d(in_channels=8, out_channels=16, kernel_size=(1,1), stride=(2,2), bias=True,padding = 0)
        # conv5_2_, output shape: {[16,16,16]}

        self.batchnorm5_2_ = nn.BatchNorm2d(num_features=16)
        # batchnorm5_2_, output shape: {[16,16,16]}


        self.relu7_ = nn.ReLU()

        self.conv8_1_ = nn.Conv2d(in_channels=16, out_channels=16, kernel_size=(3,3), stride=(1,1), bias=True,padding = 'same')
        # conv8_1_, output shape: {[16,16,16]}

        self.batchnorm8_1_ = nn.BatchNorm2d(num_features=16)
        # batchnorm8_1_, output shape: {[16,16,16]}

        self.relu8_1_ = nn.ReLU()

        self.conv9_1_ = nn.Conv2d(in_channels=16, out_channels=16, kernel_size=(3,3), stride=(1,1), bias=True,padding = 'same')
        # conv9_1_, output shape: {[16,16,16]}

        self.batchnorm9_1_ = nn.BatchNorm2d(num_features=16)
        # batchnorm9_1_, output shape: {[16,16,16]}


        self.relu10_ = nn.ReLU()

        self.conv11_1_ = nn.Conv2d(in_channels=16, out_channels=16, kernel_size=(3,3), stride=(1,1), bias=True,padding = 'same')
        # conv11_1_, output shape: {[16,16,16]}

        self.batchnorm11_1_ = nn.BatchNorm2d(num_features=16)
        # batchnorm11_1_, output shape: {[16,16,16]}

        self.relu11_1_ = nn.ReLU()

        self.conv12_1_ = nn.Conv2d(in_channels=16, out_channels=16, kernel_size=(3,3), stride=(1,1), bias=True,padding = 'same')
        # conv12_1_, output shape: {[16,16,16]}

        self.batchnorm12_1_ = nn.BatchNorm2d(num_features=16)
        # batchnorm12_1_, output shape: {[16,16,16]}


        self.relu13_ = nn.ReLU()

        self.conv14_1_ = nn.Conv2d(in_channels=16, out_channels=32, kernel_size=(3,3), stride=(2,2), bias=True,padding = 1)
        # conv14_1_, output shape: {[32,8,8]}

        self.batchnorm14_1_ = nn.BatchNorm2d(num_features=32)
        # batchnorm14_1_, output shape: {[32,8,8]}

        self.relu14_1_ = nn.ReLU()

        self.conv15_1_ = nn.Conv2d(in_channels=32, out_channels=32, kernel_size=(3,3), stride=(1,1), bias=True,padding = 'same')
        # conv15_1_, output shape: {[32,8,8]}

        self.batchnorm15_1_ = nn.BatchNorm2d(num_features=32)
        # batchnorm15_1_, output shape: {[32,8,8]}

        self.conv14_2_ = nn.Conv2d(in_channels=16, out_channels=32, kernel_size=(1,1), stride=(2,2), bias=True,padding = 0)
        # conv14_2_, output shape: {[32,8,8]}

        self.batchnorm14_2_ = nn.BatchNorm2d(num_features=32)
        # batchnorm14_2_, output shape: {[32,8,8]}


        self.relu16_ = nn.ReLU()

        self.conv17_1_ = nn.Conv2d(in_channels=32, out_channels=32, kernel_size=(3,3), stride=(1,1), bias=True,padding = 'same')
        # conv17_1_, output shape: {[32,8,8]}

        self.batchnorm17_1_ = nn.BatchNorm2d(num_features=32)
        # batchnorm17_1_, output shape: {[32,8,8]}

        self.relu17_1_ = nn.ReLU()

        self.conv18_1_ = nn.Conv2d(in_channels=32, out_channels=32, kernel_size=(3,3), stride=(1,1), bias=True,padding = 'same')
        # conv18_1_, output shape: {[32,8,8]}

        self.batchnorm18_1_ = nn.BatchNorm2d(num_features=32)
        # batchnorm18_1_, output shape: {[32,8,8]}


        self.relu19_ = nn.ReLU()

        self.conv20_1_ = nn.Conv2d(in_channels=32, out_channels=32, kernel_size=(3,3), stride=(1,1), bias=True,padding = 'same')
        # conv20_1_, output shape: {[32,8,8]}

        self.batchnorm20_1_ = nn.BatchNorm2d(num_features=32)
        # batchnorm20_1_, output shape: {[32,8,8]}

        self.relu20_1_ = nn.ReLU()

        self.conv21_1_ = nn.Conv2d(in_channels=32, out_channels=32, kernel_size=(3,3), stride=(1,1), bias=True,padding = 'same')
        # conv21_1_, output shape: {[32,8,8]}

        self.batchnorm21_1_ = nn.BatchNorm2d(num_features=32)
        # batchnorm21_1_, output shape: {[32,8,8]}


        self.relu22_ = nn.ReLU()

        self.conv23_1_ = nn.Conv2d(in_channels=32, out_channels=64, kernel_size=(3,3), stride=(2,2), bias=True,padding = 1)
        # conv23_1_, output shape: {[64,4,4]}

        self.batchnorm23_1_ = nn.BatchNorm2d(num_features=64)
        # batchnorm23_1_, output shape: {[64,4,4]}

        self.relu23_1_ = nn.ReLU()

        self.conv24_1_ = nn.Conv2d(in_channels=64, out_channels=64, kernel_size=(3,3), stride=(1,1), bias=True,padding = 'same')
        # conv24_1_, output shape: {[64,4,4]}

        self.batchnorm24_1_ = nn.BatchNorm2d(num_features=64)
        # batchnorm24_1_, output shape: {[64,4,4]}

        self.conv23_2_ = nn.Conv2d(in_channels=32, out_channels=64, kernel_size=(1,1), stride=(2,2), bias=True,padding = 0)
        # conv23_2_, output shape: {[64,4,4]}

        self.batchnorm23_2_ = nn.BatchNorm2d(num_features=64)
        # batchnorm23_2_, output shape: {[64,4,4]}


        self.relu25_ = nn.ReLU()

        self.conv26_1_ = nn.Conv2d(in_channels=64, out_channels=64, kernel_size=(3,3), stride=(1,1), bias=True,padding = 'same')
        # conv26_1_, output shape: {[64,4,4]}

        self.batchnorm26_1_ = nn.BatchNorm2d(num_features=64)
        # batchnorm26_1_, output shape: {[64,4,4]}

        self.relu26_1_ = nn.ReLU()

        self.conv27_1_ = nn.Conv2d(in_channels=64, out_channels=64, kernel_size=(3,3), stride=(1,1), bias=True,padding = 'same')
        # conv27_1_, output shape: {[64,4,4]}

        self.batchnorm27_1_ = nn.BatchNorm2d(num_features=64)
        # batchnorm27_1_, output shape: {[64,4,4]}


        self.relu28_ = nn.ReLU()

        self.conv29_1_ = nn.Conv2d(in_channels=64, out_channels=64, kernel_size=(3,3), stride=(1,1), bias=True,padding = 'same')
        # conv29_1_, output shape: {[64,4,4]}

        self.batchnorm29_1_ = nn.BatchNorm2d(num_features=64)
        # batchnorm29_1_, output shape: {[64,4,4]}

        self.relu29_1_ = nn.ReLU()

        self.conv30_1_ = nn.Conv2d(in_channels=64, out_channels=64, kernel_size=(3,3), stride=(1,1), bias=True,padding = 'same')
        # conv30_1_, output shape: {[64,4,4]}

        self.batchnorm30_1_ = nn.BatchNorm2d(num_features=64)
        # batchnorm30_1_, output shape: {[64,4,4]}


        self.relu31_ = nn.ReLU()

        self.globalpooling31_ = nn.AdaptiveAvgPool2d((1, 1))
        # globalpooling31_, output shape: {[64,1,1]}

        self.fc31_ = nn.Linear(in_features=64, out_features=128,bias=True)
        # fc31_, output shape: {[128,1,1]}

        self.dropout31_ = nn.Dropout(p=0.5)

        self.fc32_ = nn.Linear(in_features=128, out_features=10,bias=True)
        # fc32_, output shape: {[10,1,1]}

        self.softmax_ = nn.Identity()

        pass

    def forward(self , data_ ):

        conv2_1_ = self.conv2_1_(data_)

        batchnorm2_1_ = self.batchnorm2_1_(conv2_1_)

        relu2_1_ = self.relu2_1_(batchnorm2_1_)

        conv3_1_ = self.conv3_1_(relu2_1_)

        batchnorm3_1_ = self.batchnorm3_1_(conv3_1_)

        conv2_2_ = self.conv2_2_(data_)

        batchnorm2_2_ = self.batchnorm2_2_(conv2_2_)

        add4_ = batchnorm3_1_ + batchnorm2_2_
        # add4_, output shape: {[8,32,32]}

        relu4_ = self.relu4_(add4_)

        conv5_1_ = self.conv5_1_(relu4_)

        batchnorm5_1_ = self.batchnorm5_1_(conv5_1_)

        relu5_1_ = self.relu5_1_(batchnorm5_1_)

        conv6_1_ = self.conv6_1_(relu5_1_)

        batchnorm6_1_ = self.batchnorm6_1_(conv6_1_)

        conv5_2_ = self.conv5_2_(relu4_)

        batchnorm5_2_ = self.batchnorm5_2_(conv5_2_)

        add7_ = batchnorm6_1_ + batchnorm5_2_
        # add7_, output shape: {[16,16,16]}

        relu7_ = self.relu7_(add7_)

        conv8_1_ = self.conv8_1_(relu7_)

        batchnorm8_1_ = self.batchnorm8_1_(conv8_1_)

        relu8_1_ = self.relu8_1_(batchnorm8_1_)

        conv9_1_ = self.conv9_1_(relu8_1_)

        batchnorm9_1_ = self.batchnorm9_1_(conv9_1_)

        add10_ = batchnorm9_1_ + relu7_
        # add10_, output shape: {[16,16,16]}

        relu10_ = self.relu10_(add10_)

        conv11_1_ = self.conv11_1_(relu10_)

        batchnorm11_1_ = self.batchnorm11_1_(conv11_1_)

        relu11_1_ = self.relu11_1_(batchnorm11_1_)

        conv12_1_ = self.conv12_1_(relu11_1_)

        batchnorm12_1_ = self.batchnorm12_1_(conv12_1_)

        add13_ = batchnorm12_1_ + relu10_
        # add13_, output shape: {[16,16,16]}

        relu13_ = self.relu13_(add13_)

        conv14_1_ = self.conv14_1_(relu13_)

        batchnorm14_1_ = self.batchnorm14_1_(conv14_1_)

        relu14_1_ = self.relu14_1_(batchnorm14_1_)

        conv15_1_ = self.conv15_1_(relu14_1_)

        batchnorm15_1_ = self.batchnorm15_1_(conv15_1_)

        conv14_2_ = self.conv14_2_(relu13_)

        batchnorm14_2_ = self.batchnorm14_2_(conv14_2_)

        add16_ = batchnorm15_1_ + batchnorm14_2_
        # add16_, output shape: {[32,8,8]}

        relu16_ = self.relu16_(add16_)

        conv17_1_ = self.conv17_1_(relu16_)

        batchnorm17_1_ = self.batchnorm17_1_(conv17_1_)

        relu17_1_ = self.relu17_1_(batchnorm17_1_)

        conv18_1_ = self.conv18_1_(relu17_1_)

        batchnorm18_1_ = self.batchnorm18_1_(conv18_1_)

        add19_ = batchnorm18_1_ + relu16_
        # add19_, output shape: {[32,8,8]}

        relu19_ = self.relu19_(add19_)

        conv20_1_ = self.conv20_1_(relu19_)

        batchnorm20_1_ = self.batchnorm20_1_(conv20_1_)

        relu20_1_ = self.relu20_1_(batchnorm20_1_)

        conv21_1_ = self.conv21_1_(relu20_1_)

        batchnorm21_1_ = self.batchnorm21_1_(conv21_1_)

        add22_ = batchnorm21_1_ + relu19_
        # add22_, output shape: {[32,8,8]}

        relu22_ = self.relu22_(add22_)

        conv23_1_ = self.conv23_1_(relu22_)

        batchnorm23_1_ = self.batchnorm23_1_(conv23_1_)

        relu23_1_ = self.relu23_1_(batchnorm23_1_)

        conv24_1_ = self.conv24_1_(relu23_1_)

        batchnorm24_1_ = self.batchnorm24_1_(conv24_1_)

        conv23_2_ = self.conv23_2_(relu22_)

        batchnorm23_2_ = self.batchnorm23_2_(conv23_2_)

        add25_ = batchnorm24_1_ + batchnorm23_2_
        # add25_, output shape: {[64,4,4]}

        relu25_ = self.relu25_(add25_)

        conv26_1_ = self.conv26_1_(relu25_)

        batchnorm26_1_ = self.batchnorm26_1_(conv26_1_)

        relu26_1_ = self.relu26_1_(batchnorm26_1_)

        conv27_1_ = self.conv27_1_(relu26_1_)

        batchnorm27_1_ = self.batchnorm27_1_(conv27_1_)

        add28_ = batchnorm27_1_ + relu25_
        # add28_, output shape: {[64,4,4]}

        relu28_ = self.relu28_(add28_)

        conv29_1_ = self.conv29_1_(relu28_)

        batchnorm29_1_ = self.batchnorm29_1_(conv29_1_)

        relu29_1_ = self.relu29_1_(batchnorm29_1_)

        conv30_1_ = self.conv30_1_(relu29_1_)

        batchnorm30_1_ = self.batchnorm30_1_(conv30_1_)

        add31_ = batchnorm30_1_ + relu28_
        # add31_, output shape: {[64,4,4]}

        relu31_ = self.relu31_(add31_)

        globalpooling31_ = self.globalpooling31_(relu31_)
        globalpooling31_ = globalpooling31_.reshape(globalpooling31_.shape[0], -1)

        fc31_ = self.fc31_(globalpooling31_)

        dropout31_ = self.dropout31_(fc31_)

        fc32_ = self.fc32_(dropout31_)
        softmax32_  = F.log_softmax(fc32_, dim=-1)

        softmax_ = self.softmax_(softmax32_)

        return softmax_


