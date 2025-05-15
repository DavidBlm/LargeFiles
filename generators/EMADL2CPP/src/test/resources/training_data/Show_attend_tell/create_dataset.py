# (c) https://github.com/MontiCore/monticore  
from __future__ import absolute_import, division, print_function, unicode_literals
import tensorflow as tf
import numpy as np
import os
import json
from glob import glob
import pickle
import cv2
import h5py
import string, re
import random



# Half of this goes into train.h5, the other half into test.h5
number_of_images = 20000

# Max length of sentences
max_length = 25

# Shuffle data?
shuffle = True

datasets_to_create = ["train.h5", "test.h5"]


# Download COCO dataset
annotation_file = "./annotations/captions_train2014.json"
annotation_zip = tf.keras.utils.get_file("captions.zip",
                                          cache_subdir=os.path.abspath("."),
                                          origin = "http://images.cocodataset.org/annotations/annotations_trainval2014.zip",
                                          extract = True)

name_of_zip = "train2014.zip"
if not os.path.exists(os.path.abspath(".") + "/" + name_of_zip):
  image_zip = tf.keras.utils.get_file(name_of_zip,
                                      cache_subdir=os.path.abspath("."),
                                      origin = "http://images.cocodataset.org/zips/train2014.zip",
                                      extract = True)
  PATH = os.path.dirname(image_zip)+"/train2014/"
else:
  PATH = os.path.abspath(".")+"/train2014/"


with open(annotation_file, "r") as f:
    annotations = json.load(f)

# Shuffle
annotations = list(annotations["annotations"])
if shuffle:
    random.shuffle(annotations)

all_captions = []
all_img_name_vector = []
all_ids = set()

# Load captions and images
for annot in annotations:
    caption = "<start> " + annot["caption"] + " <end>"
    image_id = annot["image_id"]
    if image_id not in all_ids:
        all_ids.add(image_id)
        full_coco_image_path = PATH + "COCO_train2014_" + "%012d.jpg" % (image_id)

        all_img_name_vector.append(full_coco_image_path)
        all_captions.append(caption)
        

# Get the amount of data we want
captions = all_captions[:number_of_images]
img_name_vector = all_img_name_vector[:number_of_images]


# Load Inception V3 model
image_model = tf.keras.applications.InceptionV3(include_top=False, weights="imagenet")
new_input = image_model.input
hidden_layer = image_model.layers[-1].output
image_features_extract_model = tf.keras.Model(new_input, hidden_layer)


def load_image(image_path):
    img = tf.io.read_file(image_path)
    img = tf.image.decode_jpeg(img, channels=3)
    img = tf.image.resize(img, (299, 299))
    img = tf.keras.applications.inception_v3.preprocess_input(img)
    return img, image_path


# Map images to features using pretrained Inception V3
image_dataset = tf.data.Dataset.from_tensor_slices(img_name_vector)
image_dataset = image_dataset.map(
  load_image, num_parallel_calls=tf.data.experimental.AUTOTUNE).batch(16)


# Save extracted features
feature_tensors=[]
for img, path in image_dataset:
  batch_features = image_features_extract_model(img)
  batch_features = tf.reshape(batch_features,
                              (batch_features.shape[0], -1, batch_features.shape[3]))
  for i in range(batch_features.shape[0]):
    feature_tensors.append(batch_features[i][:][:].numpy())
    
#eliminate dots and commas with this regex
regex = re.compile("[%s]" % re.escape(string.punctuation))

# Create vocabulary from all words used in captions, and create labels
vocabulary = {"<start>": 0, "<end>": 1, "<pad>": 2}
labels = []
for entry in captions:
	sentence = []
	for word in entry.split(" "):
		if word not in ["<start>", "<end>", "<pad>"]:
			word = regex.sub("", word)
		if word.lower() not in vocabulary:
			vocabulary[word.lower()] = len(vocabulary);
			string = str(len(vocabulary)-1) + ": " + word.lower()
		sentence.append(vocabulary[word.lower()])
	labels.append(sentence)


# Save dict
VocabularyInverse = dict((v,k) for k,v in vocabulary.items())
with open("dict.pkl", "wb") as f:
	pickle.dump(VocabularyInverse, f, 2)

with open('dict.txt', 'w') as f:
    for item in VocabularyInverse.values():
        f.write("%s\n" % item)

# Pad to max length
for sentence in labels:
    sentence += [2] * (max_length - len(sentence))


# Save images in a list
images=[]
for i in range(len(img_name_vector)):
    img = cv2.imread(img_name_vector[i])
    res = cv2.resize(img, dsize=(224, 224), interpolation=cv2.INTER_CUBIC)
    res = cv2.cvtColor(res, cv2.COLOR_BGR2RGB)
    res = res.transpose(2,0,1)
    images.append(res)


# Save .h5 files
for index, file in enumerate(datasets_to_create):

    # train set
    if index == 0:
        captions_temp = labels[0:len(labels)//2]
        features = feature_tensors[0:len(labels)//2]
        images_temp = images[0:len(labels)//2]
    # test set
    else:
        captions_temp = labels[len(labels)//2:len(labels)]
        features = feature_tensors[len(labels)//2:len(labels)]
        images_temp = images[len(labels)//2:len(labels)]

    train_shape = (len(captions_temp), 64,2048)
    image_shape = (len(captions_temp), 3,224,224)

    hdf5_file = h5py.File(file, mode="w")
    hdf5_file.create_dataset("data", train_shape, np.float32)
    hdf5_file.create_dataset("images", image_shape, np.uint8)

    for index in range(max_length):
        np_labels = np.array([label[index] for label in labels], dtype=np.int32)
        hdf5_file.create_dataset("target_{}_label".format(index), data = np_labels, dtype=np.int32)

    for i in range(len(captions_temp)):
        if i % 1000 == 0 and i > 1:
            print("Processed: {}/{}".format(i, len(captions_temp)))
        hdf5_file["data"][i, ...] = features[i]
        hdf5_file["images"][i, ...] = images_temp[i]
    hdf5_file.close()

