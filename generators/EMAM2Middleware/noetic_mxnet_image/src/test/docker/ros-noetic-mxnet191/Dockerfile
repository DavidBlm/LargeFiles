FROM ros:noetic-ros-base-focal

ENV DEBIAN_FRONTEND=noninteractive
ENV TZ="Europe/Berlin"

RUN apt update && apt install -y lsb-release build-essential maven openjdk-8-jdk cmake ccache git ninja-build libopenblas-dev \
    libopencv-dev libarmadillo-dev python3-dev python3-numpy python3-pip python-is-python3 curl swig tmux

RUN update-alternatives --set java /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java

# build and install mxnet cpp
RUN git clone --recursive https://github.com/apache/incubator-mxnet.git /mxnet && \
    cd /mxnet && git checkout tags/1.9.1 && git submodule update --recursive --init && \
    mkdir build && cd build && \
    cmake -DUSE_CPP_PACKAGE=1 -DUSE_CUDA=0 -DUSE_OPENCV=1 -DBUILD_CPP_EXAMPLES=0 -DUSE_OPERATOR_TUNING=1 -DUSE_GPERFTOOLS=0 -DUSE_JEMALLOC=0 -DUSE_SSE=1 -DUSE_F16C=0 -GNinja .. && \
    ninja && ninja install && \
    rm -rf /mxnet

RUN pip install pyprind h5py 'matplotlib<3.7.0' 'numpy>=1.19.0,<1.20.0' mxnet==1.9.1 gym[all]

RUN mkdir -p /root/.config/matplotlib && \
    echo "backend : Agg" > /root/.config/matplotlib/matplotlibrc