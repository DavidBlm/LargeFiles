FROM registry.git.rwth-aachen.de/monticore/embeddedmontiarc/generators/emadl2cpp/integrationtests/mxnet:v0.0.3

RUN pip install opencv-python

# Install Open CV - Warning, this takes absolutely forever
RUN apt-get update && apt-get install -y libgdal-dev
RUN mkdir -p ~/opencv cd ~/opencv && \
    wget https://github.com/Itseez/opencv/archive/3.0.0.zip && \
    unzip 3.0.0.zip && \
    rm 3.0.0.zip && \
    mv opencv-3.0.0 OpenCV
RUN apt-get install -y qtbase5-dev
RUN apt-get install -y build-essential
ENV PATH="${PATH}:/usr/include/"
RUN cd OpenCV && \
    mkdir build && \ 
    cd build && \
    cmake \
    -DWITH_QT=ON \ 
    -DWITH_OPENGL=ON \ 
    -DFORCE_VTK=ON \
    -DWITH_TBB=ON \
    -DWITH_GDAL=ON \
    -DWITH_XINE=ON \
    -DBUILD_EXAMPLES=ON \
    -D ENABLE_PRECOMPILED_HEADERS=OFF ..
RUN cd OpenCV/build && make -j4
RUN cd OpenCV/build && make install
RUN cd OpenCV/build && ldconfig

RUN apt-get install -y libopenblas-dev