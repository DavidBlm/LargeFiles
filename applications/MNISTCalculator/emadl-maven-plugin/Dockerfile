ARG BASE_IMAGE=registry.git.rwth-aachen.de/monticore/embeddedmontiarc/generators/emadl2cpp/mxnet/190:v0.0.2
FROM $BASE_IMAGE

COPY . /build/mnistcalc/emadl-maven-plugin
WORKDIR /build/mnistcalc/emadl-maven-plugin

ARG PRIVATE_TOKEN
ARG CI_JOB_TOKEN
RUN mvn -e dependency:resolve emadl:train -s settings.xml -U
RUN chmod +x calculator.sh

RUN mkdir -p input output

ENTRYPOINT [ "/bin/bash", "calculator.sh" ]