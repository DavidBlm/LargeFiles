<!-- (c) https://github.com/MontiCore/monticore -->
# MNISTCalculator

The current version for gluon is located in gluon-cpp wich uses the newest EMADL2CPP generator (v0.4.1, this is also the version located in this repository) with a full CPP MXNet/Gluon api, rather than the old c prediction api for inference which can be found in the gluon-old and mxnet folders.

## Prerequisites
1. Ubuntu Linux 16.04 LTS or 18.04 LTS (experimental) with JDK 8.
2. Deep Learning Framework **Caffe2**. [Follow the instructions from this link](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/CNNArch2Caffe2#ubuntu).
3. Deep Learning Framework **MXNet/Gluon**.
4. Deep Learning Framework **Tensorflow**.
4. Armadillo (at least armadillo version 6.600 must be used) [Official instructions at Armadillo Website](http://arma.sourceforge.net/download.html).
4. OpenCV


## MXNet/Gluon, Tensorflow and Caffe2
For detailed instructions on how to run the MXNet/Gluon, Tensorflow and Caffe2 examples, see the README in the corresponding folders.


## Build on Windows
1. Download and build this docker container: https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/applications/mnistcalculator/-/tree/ba_weber/docker/base
   2. This can take more than one hour
3. In mnistcalculator/\<your project\>/settings.xml:
   3. In the gitlab-maven part, replace the value with your private access key:
   ```
      <server>
            <id>gitlab-maven</id>
            <configuration>
                <httpHeaders>
                    <property>
                        <name>Deploy-Token</name>
                        <value>Your private access key</value>
                    </property>
                </httpHeaders>
            </configuration>
        </server>
      ```
   You can find a private access key in the first commit of the file history.
3. In <i>mnistcalculator/\<your project\></i>, run the following commands:
   ```
   docker run -d -it -p 80:3000 --name mxnetcalculator base/mxnet bash
   docker cp ..\..\mnistcalculator\ mxnetcalculator:/opt/mnistcalculator
   docker container exec -it mxnetcalculator bash -c "cd ./mnistcalculator/emadl-maven-plugin/ && mvn dependency:resolve emadl:train -s settings.xml"
   docker cp mxnetcalculator:/opt/mnistcalculator/emadl-maven-plugin/model ./model
   docker stop mxnetcalculator
   docker rm mxnetcalculator
   ```
## AutoML Pipeline
More information about the AutoML pipeline can be found in [AutoML.md](AutoML.md)
