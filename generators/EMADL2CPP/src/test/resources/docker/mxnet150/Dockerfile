FROM maven:3-jdk-8

RUN apt-get update
RUN apt-get install -y build-essential git ninja-build ccache 
RUN apt-get install -y libopenblas-dev libblas-dev 
RUN apt-get install -y liblapack-dev libopencv-dev 
RUN apt-get install -y libarmadillo-dev cmake 
RUN apt-get install -y python2.7 python-dev python-tk
RUN apt-get install -y python-numpy
RUN apt-get install -y python3-pip
RUN apt-get install -y swig unzip 
RUN apt-get install -y libboost-all-dev

ADD armadillo-9.600.6.zip /root/armadillo.zip
RUN unzip /root/armadillo.zip -d /root/armadillo
RUN cd /root/armadillo/armadillo-9.600.6 && cmake . && make && make install

RUN git clone --recursive https://github.com/apache/incubator-mxnet.git mxnet
RUN cd mxnet && git checkout tags/1.5.0 && git submodule update --recursive --init
RUN cd mxnet && mkdir build && cd build && cmake -DUSE_CPP_PACKAGE=1 -DUSE_CUDA=0 -GNinja .. && ninja
RUN cd mxnet && cp -r include/mxnet /usr/include/mxnet && cp -r cpp-package/include/mxnet-cpp /usr/include/ && cp -r 3rdparty/tvm/nnvm/include/nnvm /usr/include/ && cp -r 3rdparty/dmlc-core/include/dmlc /usr/include/

RUN pip3 install --user --upgrade "cmake>=3.13.2"

RUN wget https://bootstrap.pypa.io/get-pip.py
RUN python get-pip.py
RUN pip install --user h5py matplotlib numpy==1.16.5 mxnet==1.5.1.post0

RUN mkdir -p /root/.config/matplotlib
RUN echo "backend : Agg" > /root/.config/matplotlib/matplotlibrc
