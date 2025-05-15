FROM ubuntu:20.04

WORKDIR /preprocessing

COPY requirements.txt .

ENV DGLBACKEND=mxnet

RUN apt-get update && apt-get -y install python3 git curl python3-pip nano

RUN pip3 install setuptools numpy pybind11

RUN pip3 install -r requirements.txt

RUN pip3 install dgl dglgo -f https://data.dgl.ai/wheels/repo.html