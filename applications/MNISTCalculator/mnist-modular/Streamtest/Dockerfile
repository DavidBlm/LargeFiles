ARG BASE_IMAGE=base/mxnet
FROM $BASE_IMAGE

COPY . /build/mnistcalc/emadl-maven-plugin
WORKDIR /build/mnistcalc/emadl-maven-plugin

ARG PRIVATE_TOKEN
ARG CI_JOB_TOKEN
RUN mvn -e dependency:resolve emadl:train -s settings.xml -U
RUN chmod +x calculator.sh

RUN mkdir -p input output

ENTRYPOINT [ "/bin/bash", "calculator.sh" ]