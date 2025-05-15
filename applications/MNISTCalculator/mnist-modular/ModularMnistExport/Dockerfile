ARG BASE_IMAGE=registry.git.rwth-aachen.de/monticore/embeddedmontiarc/generators/emadl2cpp/mxnet/190:v0.0.2
FROM $BASE_IMAGE

COPY . /build/MnistExport
WORKDIR /build/MnistExport

RUN   export TZ=Europe/Berlin
RUN   apt-get update && DEBIAN_FRONTEND=noninteractive apt-get install -y tzdata libomp-dev libopencv-dev python3-opencv
RUN   ln -fs /usr/share/zoneinfo/Europe/Berlin /etc/localtime
RUN   dpkg-reconfigure --frontend noninteractive tzdata
RUN   python3 -m pip install --upgrade pip
RUN   python3 -m pip install scikit-image opencv-python
RUN   ln -fs /usr/bin/python3 /usr/bin/python



RUN mvn -e dependency:resolve -s settings.xml -U -f pom_build.xml

RUN mvn -e emadl:train -s settings.xml -U -f pom_build.xml

RUN mvn -e install -s settings.xml -f pom_deploy.xml


ENTRYPOINT [ "/bin/bash", "calculator.sh" ]