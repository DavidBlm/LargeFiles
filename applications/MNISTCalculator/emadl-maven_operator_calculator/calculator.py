import argparse
import pathlib
import random
import typing as t
import warnings

import h5py
import mxnet as mx
import mxnet.ndarray as nd
import numpy as np
from matplotlib import image
from matplotlib import pyplot as plt
from mxnet import gluon, nd


def load_h5(test_file: pathlib.Path) -> np.ndarray:
    test = h5py.File(test_file, "r")
    return np.array(test["data"]), np.array(test["softmax_label"])


def save_image(input_img: t.List[np.ndarray], label: t.List[int], prediction) -> None:
    # Save the input images
    n: int = len(input_img)
    f = plt.figure(figsize=(6, 2))
    for i in range(n):
        # Debug, plot figure
        f.add_subplot(1, n, i + 1)
        plt.imshow(input_img[i][0])

    plt.suptitle(f"\nLabel: {label}\nPrediction: {prediction}")
    plt.show(block=True)
    plt.savefig("output/img.png")


def get_prediction(net: gluon.block.Block, input, label) -> int:
    # Display the predictions
    data = mx.nd.array(input)
    out = net(data)
    predictions = nd.argmax(out, axis=1)

    return int(predictions.asnumpy()[0]), int(label[0])


if __name__ == "__main__":
    # Use GPU if one exists, else use CPU
    ctx = mx.gpu() if mx.context.num_gpus() else mx.cpu()

    # load model from saved files in /emadl-maven-plugin/model
    with warnings.catch_warnings():
        warnings.simplefilter("ignore")
        deserialized_net = gluon.nn.SymbolBlock.imports(
            "model/calculator.Network/newest-symbol.json",
            ["data"],
            "model/calculator.Network/newest-0000.params",
            ctx=ctx,
        )
        print("\n\t\t------------ Start Prediction ------------\n")

    # Init parser
    parser = argparse.ArgumentParser()

    # Add arguments
    parser.add_argument(
        "--image_input",
        type=str,
        required=False,
        help="Use own input image for prediction. Pass 6 paths to input images, saved in input_images/ folder named as [0-9].png.",
    )
    args = parser.parse_args()

    if args.image_input:
        # get image array and labels from input paths
        data = args.image_input.split()
        input_list = [np.array([image.imread(path)]) for path in data]
        label_list = [np.array([int(pathlib.Path(path).stem)]) for path in data]

        digits = []
        for i in range(0, 6):
            # input format should've shape (1, 28, 28)
            digits.append(
                get_prediction(deserialized_net, [input_list[i]], [label_list[i]])[0]
            )
            label_list[i] = label_list[i][0]

        num_1 = int(f"{digits[0]}{digits[1]}{digits[2]}")
        num_2 = int(f"{digits[3]}{digits[4]}{digits[5]}")

    else:
        # default method
        _data_dir = (
            pathlib.Path().home()
            / ".m2"
            / "repository"
            / "de"
            / "monticore"
            / "lang"
            / "monticar"
            / "datasets"
            / "mnist-operators"
            / "1"
            / "mnist-operators-1-dataset"
            / "training_data"
            / "test.h5"
        )
        data, label = load_h5(_data_dir)

        index = [random.randint(0, 9999) for i in range(0, 6)]

        input_list = []
        label_list = []
        digits = []
        for i in range(0, 6):
            input_list.append(data[index[i]])
            label_list.append(int(label[index[i]]))
            # input format should've shape (1, 28, 28)
            digits.append(
                get_prediction(deserialized_net, [data[index[i]]], [label[index[i]]])[0]
            )

        num_1 = int(f"{digits[0]}{digits[1]}{digits[2]}")
        num_2 = int(f"{digits[3]}{digits[4]}{digits[5]}")

    # save images as png
    save_image(input_list, label_list, digits)

    # print results
    print(f"\t\t-- Input:\t\t{label_list}")
    print("\t\t-- Output:")
    print(f"  \t\t - MNISTDetector:\t{digits}")
    print(f"  \t\t - Compose Number:\t{num_1} and {num_2}")
    print(f"  \t\t - SUM:\t\t\t{num_1 + num_2}\n")

    #Determine the accuracy of the Neural Networks
    #1000 Tests
    correct = 0
    for i in range(0,1000):
        rand = random.randint(0, 9999)
        predicted = get_prediction(deserialized_net,[data[rand]],[label[rand]])
        if predicted[0] == predicted[1]:
            correct = correct + 1
    print(f"Accuracy = {correct / 1000:.3f}")
