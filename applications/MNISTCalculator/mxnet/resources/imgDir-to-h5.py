# (c) https://github.com/MontiCore/monticore  
from __future__ import print_function
from __future__ import division
import h5py
import numpy as np
import cv2
import os
import argparse
import errno
import random
import sys


def create_img_list(name, data_path):
    dir_name = data_path + "/" + name

    image_paths = []
    image_class_indices = []
    print(dir_name)
    for class_index_name in os.listdir(dir_name):
        class_dir_path = dir_name + "/" + class_index_name
        if os.path.isdir(class_dir_path):
            for image_name in os.listdir(class_dir_path):
                image_path = class_dir_path + "/" + image_name
                image_paths.append(image_path)
                class_index = float(class_index_name)
                image_class_indices.append(class_index)
    return image_paths, image_class_indices

def create_h5_from_list(image_paths, image_class_indices, target_dir, target_file_name, input_port_name, output_port_name, shuffle=True):
    img = cv2.imread(image_paths[0])
    t_img = np.transpose(img, (2,0,1)).astype(np.float32)
    #t_img = t_img[-1:,:,:]
    #print(t_img)
    channels = t_img.shape[0]
    height = t_img.shape[1]
    width = t_img.shape[2]
    data_size = len(image_paths)
    
    target_file = target_dir + "/" + target_file_name + ".h5"
    if os.path.isfile(target_file):
        print("File", target_file, "already exists. Skipping data file creation.")
        return
    try:
        os.makedirs(target_dir)
    except OSError as e:
        if e.errno != errno.EEXIST:
            raise

    if shuffle:
        combined = list(zip(image_paths, image_class_indices))
        random.shuffle(combined)
        image_paths[:], image_class_indices[:] = zip(*combined)
    
    print("Creating " + target_file + " (images:" + str(data_size) + ", channels:" + str(channels) + ", height:" + str(height) + ", width:" + str(width) + "):")
    with h5py.File(target_file, "w") as ofile:
        in_dset = ofile.create_dataset(input_port_name, (data_size,channels, height, width), dtype=np.float32)
        out_dset = ofile.create_dataset(output_port_name + "_label", (data_size,), dtype=np.float32)
        for i in range(data_size):
            img = cv2.imread(image_paths[i])
            t_img = np.transpose(img, (2,0,1)).astype(np.float32)
            #t_img = t_img[-1:,:,:]
            in_dset[i] = t_img
            out_dset[i] = image_class_indices[i]

            #print progress
            if i % 100 == 0:
                percentage = 100*i / data_size
                sys.stdout.write("\r{:0.1f}%".format(percentage))
                sys.stdout.flush()
    sys.stdout.write("\r100.0%\n")
    sys.stdout.flush()


            
if __name__ == "__main__":
    parser = argparse.ArgumentParser(description='Translate image directories into hdf5 training sets for EMADL.')
    parser.add_argument("--in_port", action="store", dest="in_port", default="data")
    parser.add_argument("--out_port", action="store", dest="out_port", default="softmax")
    parser.add_argument("--data_path", action="store", dest="data_path", default=".")
    parser.add_argument("--target_path", action="store", dest="target_path", default=".")
    args = parser.parse_args()
    for file_name in os.listdir(args.data_path):
        if file_name == "train":
            image_paths, image_class_indices = create_img_list(file_name, args.data_path)
            create_h5_from_list(image_paths, image_class_indices, args.target_path, file_name, args.in_port, args.out_port)
        if file_name == "test":
            image_paths, image_class_indices = create_img_list(file_name, args.data_path)
            create_h5_from_list(image_paths, image_class_indices, args.target_path, file_name, args.in_port, args.out_port)
