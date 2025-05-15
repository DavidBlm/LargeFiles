FROM floopcz/tensorflow_cc:ubuntu-shared

#Install everything needed with apt
RUN apt-get update
ARG DEBIAN_FRONTEND=noninteractive
RUN apt-get install -y --no-install-recommends \
        tzdata \
        git \
        wget python gcc \
        build-essential cmake \
        maven unzip\
        python3 python3-pip python-dev python-setuptools python python-pip\
        libopencv-dev libarmadillo-dev

#Fix Java version
RUN apt remove -y *openjdk*
RUN apt-get install -y --no-install-recommends openjdk-8-jdk

#Install stuff for python and python3
RUN pip install tensorflow==1.13.1
RUN pip3 install opencv-python h5py numpy matplotlib scipy Pillow
