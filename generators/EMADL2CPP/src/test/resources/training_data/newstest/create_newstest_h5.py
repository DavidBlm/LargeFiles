# (c) https://github.com/MontiCore/monticore  
import h5py
import numpy as np
import os.path
import sys

# Config
MAX_LENGTH = 30

PAD_TOKEN = '<pad>'
UNK_TOKEN = '<unk>'
SOS_TOKEN = '<s>'
EOS_TOKEN = '</s>'

FILES = {
    'vocab_source': 'vocab.50K.en',
    'vocab_target': 'vocab.50K.de',
    'train_source': 'train.en',
    'train_target': 'train.de',
    'test_source': 'newstest2014.en',
    'test_target': 'newstest2014.de'
}

# Functions
def read_vocabulary(string):
    vocabulary = []

    for line in string.splitlines():
        vocabulary.append(line)

    return vocabulary

def check_vocabulary(vocabulary):
    return PAD_TOKEN in vocabulary \
           and UNK_TOKEN in vocabulary \
           and SOS_TOKEN in vocabulary \
           and EOS_TOKEN in vocabulary

def create_vocabulary_dict(vocabulary):
    vocabulary_dict = {}
    index = 0

    for word in vocabulary:
        vocabulary_dict[word] = index
        index += 1

    return vocabulary_dict

def read_corpus(string, vocabulary_dict):
    corpus = []

    pad_token_index = vocabulary_dict[PAD_TOKEN]
    unk_token_index = vocabulary_dict[UNK_TOKEN]
    sos_token_index = vocabulary_dict[SOS_TOKEN]
    eos_token_index = vocabulary_dict[EOS_TOKEN]

    for line in string.splitlines():
        words = line.split(' ')
        sequence = [sos_token_index]

        for word in words:
            try:
                index = vocabulary_dict[word]
            except KeyError:
                index = unk_token_index

            sequence.append(index)

        sequence.append(eos_token_index)

        # Pad sentence
        while len(sequence) < MAX_LENGTH:
            sequence.append(pad_token_index)

        corpus.append(sequence)

    return corpus


# Request files
contents = {}

for key, filename in FILES.items():
    if not os.path.isfile(filename):
        print('File ' + filename + ' does not exist')
        sys.exit()

    with open(filename, 'r') as file:
        contents[key] = file.read()

# Read vocabularies
vocab_source = read_vocabulary(contents['vocab_source'])
vocab_target = read_vocabulary(contents['vocab_target'])

# Insert <pad>, remove last word so that our vocabulary size stays the same
vocab_source.insert(0, PAD_TOKEN)
del vocab_source[-1]

vocab_target.insert(0, PAD_TOKEN)
del vocab_target[-1]

if not check_vocabulary(vocab_source):
    print('Source vocabulary is at least missing one of these words: <pad>, <unk>, <s> or </s>')
    sys.exit()

if not check_vocabulary(vocab_target):
    print('Target vocabulary is at least missing one of these words: <pad>, <unk>, <s> or </s>')
    sys.exit()

# Create id to word mapping to make reading corpora faster
vocab_source_dict = create_vocabulary_dict(vocab_source)
vocab_target_dict = create_vocabulary_dict(vocab_target)

if 'train_source' in contents and 'train_target' in contents:
    # Read train corpora
    train_source = read_corpus(contents['train_source'], vocab_source_dict)
    train_target = read_corpus(contents['train_target'], vocab_target_dict)

    if len(train_source) != len(train_target):
        print('Source and target train corpus have different length')
        sys.exit()

    # Remove sentences that are too long
    i = 0
    while i < len(train_source):
        if len(train_source[i]) > MAX_LENGTH or len(train_target[i]) > MAX_LENGTH:
            del train_source[i]
            del train_target[i]
        else:
            i += 1

    # Create train.h5
    with h5py.File('train.h5', mode='w') as train_h5:
        train_h5.create_dataset("source", (len(train_source), MAX_LENGTH), data=np.array(train_source), dtype=np.int32)

        for index in range(MAX_LENGTH):
            np_labels = np.array([sentence[index] for sentence in train_target], dtype=np.int32)
            train_h5.create_dataset("target_{}_label".format(index), data=np_labels, dtype=np.int32)

if 'test_source' in contents and 'test_target' in contents:
    # Read test corpora
    test_source = read_corpus(contents['test_source'], vocab_source_dict)
    test_target = read_corpus(contents['test_target'], vocab_target_dict)

    if len(test_source) != len(test_target):
        print('Source and target test corpus have different length')
        sys.exit()

    # Remove sentences that are too long
    i = 0
    while i < len(test_source):
        if len(test_source[i]) > MAX_LENGTH or len(test_target[i]) > MAX_LENGTH:
            del test_source[i]
            del test_target[i]
        else:
            i += 1

    # Create test.h5
    with h5py.File('test.h5', mode='w') as test_h5:
        test_h5.create_dataset("source", (len(test_source), MAX_LENGTH), data=np.array(test_source), dtype=np.int32)

        for index in range(MAX_LENGTH):
            np_labels = np.array([sentence[index] for sentence in test_target], dtype=np.int32)
            test_h5.create_dataset("target_{}_label".format(index), data=np_labels, dtype=np.int32)

