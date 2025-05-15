FROM ubuntu:18.04
SHELL ["/bin/bash", "-c"]

# Install Python, OpenJDK and build dependencies
RUN apt-get update && \
    apt-get -y upgrade && \
    apt-get -y install python3-pip openjdk-8-jdk g++ wget unzip gfortran maven git ninja-build liblapack-dev && \
    rm -rf /var/lib/apt/lists/*

# Create aliases for python and pip
RUN ln -s $(which python3) /usr/bin/python && \
    ln -s $(which pip3) /usr/bin/pip
# Change Java version to 8
RUN update-alternatives --set java /usr/lib/jvm/java-8-openjdk-amd64/jre/bin/java
# Upgrade pip
RUN python3 -m pip install --upgrade pip

# Install CMake (The version in the package manager is too old...)
WORKDIR /opt
RUN wget https://github.com/Kitware/CMake/releases/download/v3.22.3/cmake-3.22.3-linux-x86_64.sh && \
    chmod +x cmake-3.22.3-linux-x86_64.sh && \
    mkdir cmake && \
    ./cmake-3.22.3-linux-x86_64.sh --prefix=cmake --skip-license && \
    rm cmake-3.22.3-linux-x86_64.sh && \
    ln -s /opt/cmake/bin/* /usr/bin

# Install OpenCV
WORKDIR /opt
RUN wget -O opencv.zip https://github.com/opencv/opencv/archive/4.x.zip && \
    unzip opencv.zip && \
    rm opencv.zip && \
    mkdir -p opencv-4.x/build && \
    cd opencv-4.x/build && \
    cmake -GNinja .. && \
    ninja && \
    ninja install
ENV OpenCV_DIR=/opt/opencv-4.x/build/include

# Install OpenBLAS (also available in the package registry, but only an outdated version)
WORKDIR /opt
RUN wget -O openblas.zip https://sourceforge.net/projects/openblas/files/latest/download && \
    unzip openblas.zip -d openblas && \
    # Manually handle strip-components (unzip does not seem to have such an option) 
    dir=(/opt/openblas/*) && mv $dir/* $dir/.[!.]* /opt/openblas && rmdir $dir && \
    rm openblas.zip
WORKDIR /opt/openblas
RUN make && make install && \
    cp libopenblas.so.0 libopenblas.a libopenblas.so /usr/lib

# Install Armadillo
WORKDIR /opt
RUN wget -O armadillo.tar.xz https://sourceforge.net/projects/arma/files/latest/download && \
    mkdir armadillo && \
    tar xf armadillo.tar.xz -C armadillo --strip-components=1 && \
    rm armadillo.tar.xz
WORKDIR /opt/armadillo
RUN cmake . && \
    make install

# Install MXNet and H5Py
WORKDIR /opt
COPY mxnet.cmake /tmp/mxnet.cmake
RUN pip3 install mxnet h5py
RUN git clone --recursive https://github.com/apache/incubator-mxnet mxnet && \
    cd mxnet && \
    git checkout tags/1.9.0 && \
    git submodule update --recursive --init && \
    mv /tmp/mxnet.cmake config.cmake && \
    mkdir build && \
    cd build && \
    cmake -GNinja .. && \
    ninja && \
    ninja install && \
    cp libmxnet.so libmxnet.a /usr/lib

RUN pip install dgl matplotlib numpy torch
ENV DGLBACKEND=mxnet

ENV JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64/jre