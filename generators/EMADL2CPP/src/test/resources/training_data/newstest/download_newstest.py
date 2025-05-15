# (c) https://github.com/MontiCore/monticore  
import requests

urls = [
    'https://nlp.stanford.edu/projects/nmt/data/wmt14.en-de/vocab.50K.en',
    'https://nlp.stanford.edu/projects/nmt/data/wmt14.en-de/vocab.50K.de',
    'https://nlp.stanford.edu/projects/nmt/data/wmt14.en-de/train.en',
    'https://nlp.stanford.edu/projects/nmt/data/wmt14.en-de/train.de',
    'https://nlp.stanford.edu/projects/nmt/data/wmt14.en-de/newstest2014.en',
    'https://nlp.stanford.edu/projects/nmt/data/wmt14.en-de/newstest2014.de'
]

for url in urls:
    filename = url.split('/')[-1]

    with requests.get(url, stream=True) as r:
        r.raise_for_status()

        with open(filename, 'wb') as f:
            for chunk in r.iter_content(chunk_size=8192): 
                if chunk:
                    f.write(chunk)

    print(filename + ' downloaded')





