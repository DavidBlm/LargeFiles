FROM maven:3-jdk-8

RUN apt-get update
RUN apt-get install -y build-essential git ninja-build ccache 
RUN apt-get install -y libopenblas-dev libblas-dev 
RUN apt-get install -y liblapack-dev libopencv-dev 
RUN apt-get install -y libarmadillo-dev cmake 
RUN apt-get install -y libboost-all-dev

RUN apt-get install -y python3.9 python-dev python-tk
RUN apt-get install -y python3-pip

ADD armadillo-9.600.6.zip /root/armadillo.zip
RUN unzip /root/armadillo.zip -d /root/armadillo
RUN cd /root/armadillo/armadillo-9.600.6 && cmake . && make && make install

RUN pip3 install torch==1.12.0+cpu torchvision==0.13.0+cpu torchaudio==0.12.0 -f https://download.pytorch.org/whl/torch_stable.html
RUN pip3 install opencv-python h5py numpy matplotlib scipy Pillow
RUN pip3 install dgl==0.6.1

RUN pip3 install --user --upgrade "cmake>=3.13.2"

ENV DGLBACKEND=pytorch
ADD libtorch.zip /usr/lib/libtorch.zip
RUN unzip /usr/lib/libtorch.zip -d /usr/lib