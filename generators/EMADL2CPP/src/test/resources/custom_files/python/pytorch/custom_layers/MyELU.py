# (c) https://github.com/MontiCore/monticore  
import torch
import torch.nn as nn
import torch.nn.functional as F

class MyELU(nn.Module):

    def __init__(self, alpha=1.0):
        super(MyELU, self).__init__()
        self._alpha = float(alpha)

    # A method to return a dictionary with
    # the parameters of the layer as keys and their data type as value (parameters - string, types - data type objects)
    def get_parameters(self):
        return {'alpha': int}

    # A method to print a dictionary with
    # the parameters of the layer as keys and their data type as value (both should be strings)
    def print_parameters(self):
        print({'alpha': 'int'})


    # A method which gets the output dimensions of the previous layer as array of 3-element tuples and computes
    # the output dimensions of this layer and print a 5-element tuple with the output dimensions and
    # min max value in the form (channel1, height1, width1, min1, max1), (channels2, ...)
    # input_dimension_array = [(channels1, height1, width1), (channels2, height2, width2), ...]
    def compute_output_types(self, alpha, input_dimension_array):
        channels1 = input_dimension_array[0][0]
        height1 = input_dimension_array[0][1]
        width1 = input_dimension_array[0][2]
        min1 = 0
        max1 = 'oo'
        print([(channels1, height1, width1, min1, max1)])

    # How compute output function looks like if the class was predefined activation layer :
    # .channels(layer.getInputTypes().get(0).getChannels())
    # .height(layer.getInputTypes().get(0).getHeight())
    # .width(layer.getInputTypes().get(0).getWidth())
    # .elementType("0", "oo");


    def forward(self, x):
        return F.elu(x, alpha=self._alpha)
