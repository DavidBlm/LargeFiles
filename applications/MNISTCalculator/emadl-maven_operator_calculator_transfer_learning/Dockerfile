ARG BASE_IMAGE=registry.git.rwth-aachen.de/monticore/embeddedmontiarc/generators/emadl2cpp/mxnet/190:v0.0.2
FROM $BASE_IMAGE

RUN rm -rf /root/.m2

COPY . /build/mnistcalc/opTL
WORKDIR /build/mnistcalc/opTL


RUN mvn -e dependency:resolve -s settings.xml
RUN mvn -e clean verify -s settings.xml

RUN mvn  emadl:train -s settings.xml -U
RUN chmod +x calculator.sh
RUN mkdir -p input output

ENTRYPOINT [ "/bin/bash", "calculator.sh" ]
