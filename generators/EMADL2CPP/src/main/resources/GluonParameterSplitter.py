import mxnet as mx
import warnings
import sys
from mxnet import gluon

ctx = mx.cpu()
modelPath = None
paramsPath = None
network = None
oldNetwork = None
oldNetworkPath = None

newModelDirectory = None
newModelName = None
parameterLayers = None
inputNames = []
zeroInputs = []

index = 0
while index < len(sys.argv):
    arg = sys.argv[index]

    if arg == "-in":
        inputName = sys.argv[index + 1]
    elif arg == "-mp":
        modelPath = sys.argv[index + 1]
    elif arg == "-pp":
        paramsPath = sys.argv[index + 1]
    elif arg == "-nmd":
        newModelDirectory = sys.argv[index + 1]
    elif arg == "-nmn":
        newModelName = sys.argv[index + 1]
    elif arg == "-pl":
        paramList = sys.argv[index + 1]
        parameterLayers = paramList.split(",")
    elif arg == "-onp":
        oldNetworkPath = sys.argv[index + 1]
    elif arg == "-shape":
        input_shape_str = sys.argv[index + 1]
        shape_pairs = input_shape_str.split(";")
        for pair in shape_pairs:
            name, shapes = pair.split(":")
            inputNames.append(name)
            shape = tuple([int(dim) for dim in shapes.split(",")])
            zeroInputs.append(mx.nd.zeros((1, *shape), ctx=ctx))
    index += 1

with warnings.catch_warnings():
    warnings.simplefilter("ignore")
    network = gluon.nn.SymbolBlock.imports(modelPath, inputNames, paramsPath, ctx=ctx, ignore_extra=True)

if network is not None:
    file_save_dir = newModelDirectory + "/" + newModelName
    network(*zeroInputs)
    network.export(file_save_dir)

