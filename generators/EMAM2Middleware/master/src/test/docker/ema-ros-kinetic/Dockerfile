FROM ros:kinetic
RUN apt-get update
RUN apt-get install -y unzip g++
RUN apt-get install -y libopenblas-dev libhdf5-serial-dev
ENV PATH="/usr/lib/x86_64-linux-gnu/:${PATH}"
ADD https://rwth-aachen.sciebo.de/s/igDWzLpdO5zYHBj/download?path=%2Fubuntu%2F18.10.24-armadillo-linux&files=armadillo-8.500.1-linux.zip /root/armadillo.zip
RUN unzip /root/armadillo.zip -d /root/
ENV Armadillo_HOME=/root/armadillo-8.500.1-linux
ENV ROS_HOME=/opt/ros/kinetic
CMD ["bash"]
