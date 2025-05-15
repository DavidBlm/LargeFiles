<!-- (c) https://github.com/MontiCore/monticore -->
![pipeline](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/EMADL2CPP/badges/master/build.svg)
![coverage](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/EMADL2CPP/badges/master/coverage.svg)

# EMADL2CPP
Generates CPP/Python code for EmbeddedMontiArcDL, an enlargement of EmbeddedMontiArc to support Deep-Learning Components developed with MontiAnna and MontiMath Components.
Thus, is the main project that processes models of EMADL components by parsing in help of the different DSLs (EMA, CNNArchLang, ConfLang, SchemaLang, TaggingLang) and generating training and execution code by delegating the tasks to the different backends. The EMADL2CPP projects main artifact is a jar that can be invoked by hand, but there is also a maven wrapper developed which we recommend to use. 
See our Hello World example [MNIST-Calculator](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/applications/mnistcalculator/) for an exemplary application documented in [this paper](http://www.se-rwth.de/publications/Modeling-and-Training-of-Neural-Processing-Systems.pdf). Instead of calling the generation with the .jar file, we developed, as already mentioned, a [maven-plugin](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/utilities/emadl-maven-plugin), which can be seen in action in the subfolder `emadl-maven-plugin` of the MNIST-Calculator. For its invocation see the corresponding target in the .yml file.

A getting started guide for developers can be found [here](documents/getting_started.md).

[ How to develop and train a CNN component using EMADL2CPP](#nn)
<a name="nn"></a>
# Development and training of a CNN component using EMADL2CPP

## Prerequisites
* Linux. Ubuntu Linux 16.04 and 18.04 were used during testing. The docker images with everything preinstalled can be found [here](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/EMADL2CPP/container_registry). If you do not want to use docker, you will have to install the backends from on your own, the installation instructions are listed below.  
* Deep learning backends:
    * [MXNet](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/CNNArch2MXNet) (not in the focus anymore) 
        * training - generated is Python code. Required is Python 2.7 or higher, Python packages `h5py`, `mxnet`. Follow [official instructions on MXNet site](https://mxnet.incubator.apache.org/versions/1.7.0/get_started?)
        * prediction - generated code is C++
     
     * [Caffe2](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/CNNArch2Caffe2) (not in the focus anymore)
        * training - generated is Python code. Follow [ official instructions on Caffe2 site ](https://caffe2.ai/docs/getting-started.html?platform=ubuntu&configuration=prebuilt). 
        * See the scripts under Installation for better instructions, as an old caffe version is used that needs special considerations. There is no GPU support that version. It does not work with hdf5 files.
        * prediction - generated code is C++.
    
    * [Tensorflow](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/cnnarch2tensorflow) (not in the focus anymore)
	 	* training - generated is Python code. Follow [official instructions on Tensorflow site](https://www.tensorflow.org/install) for installation.
		* prediction - generated code is C++.
        
	 * [Gluon](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/CNNArch2Gluon) (imperative API for mxnet), monolithic pipeline
        * training - generated is Python code. Required is Python 2.7 or higher, Python packages `h5py`, `mxnet`. Follow [official instructions on MXNet site](https://mxnet.incubator.apache.org/versions/1.7.0/get_started?)
        * prediction - generated code is C++, but we aim at also supporting Python for execution. Individual CNN Components can be executed through using the code generated from [this template](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/CNNArch2Gluon/-/blob/master/src/main/resources/templates/gluon/CNNPythonExecuteNetwork.ftl)

    * [PyTorch](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/cnnarch2pytorch) (newest backend), flexible pipeline under development
        * training - generated is Python code. Follow [official instructions on PyTorch site](https://pytorch.org/get-started/locally/)
        * prediction - generated code is C++, but we aim at also supporting Python for execution.
	 
## Installation by hand not using Docker
A new bash script for mxnet/gluon can be found [installation scripts](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/EMADL2CPP/-/tree/master/src/main/resources/installation_scripts)
changing the installation process for mxnet for cpp (version 1.5.1). This script will install all dependencies both for python and cpp as of 26.10.2020. Note that some advanced layers need version 1.7.0 which in turn needs python3. For running the generator with python3 you may need to specifiy the python path when calling it (see howto 3.).  

## Installation using Docker
Docker Containers available under [this link](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/EMADL2CPP/container_registry).

Additionally similar docker scripts, for both versions 1.5.0 and 1.7.0, that are used for the git ci pipelines can be found in the gluon subfolder at [Docker](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/EMADL2CPP/-/tree/master/src/test/resources/docker).  
The other two bash scripts found in the installation_scripts folder are outdated but may be consulted for installation guidlines for other backends.  
Note that the installation may take some time (hours) enough RAM or a big enough swapspace is advisable (>10GB). This scripts were tested with a completly clean Ubuntu 16.04, 
without system updates installed. Using another Ubuntu version or installing other stuff, system updates included might/ have caused problems.  
If you want to install the backends with CUDA GPU support(only MXNet/Gluon and Tensorflow, the used caffe2 version does not work with GPU support anymore), 
you have to install CUDA 10.0(mxnet/ gluon also works with newer version and maybe older), CUDNN and NCCL (Obtainable from the nvidai webpage).

### HowTo
1. Define a EMADL component containing architecture of a neural network and save it in a `.emadl` file. For more information on architecture language please refer to [CNNArchLang project](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/languages/CNNArchLang). An example of NN architecture:
```
component VGG16{
    ports in Z(0:255)^{3, 224, 224} image,
         out Q(0:1)^{1000} predictions;

    implementation CNN {

        def conv(filter, channels){
            Convolution(kernel=(filter,filter), channels=channels) ->
            Relu()
        }
        def fc(){
            FullyConnected(units=4096) ->
            Relu() ->
            Dropout(p=0.5)
        }

        image ->
        conv(filter=3, channels=64, ->=2) ->
        Pooling(pool_type="max", kernel=(2,2), stride=(2,2)) ->
        conv(filter=3, channels=128, ->=2) ->
        Pooling(pool_type="max", kernel=(2,2), stride=(2,2)) ->
        conv(filter=3, channels=256, ->=3) ->
        Pooling(pool_type="max", kernel=(2,2), stride=(2,2)) ->
        conv(filter=3, channels=512, ->=3) ->
        Pooling(pool_type="max", kernel=(2,2), stride=(2,2)) ->
        conv(filter=3, channels=512, ->=3) ->
        Pooling(pool_type="max", kernel=(2,2), stride=(2,2)) ->
        fc() ->
        fc() ->
        FullyConnected(units=1000) ->
        Softmax() ->
        predictions
    }
}
```
2. Define a training configuration for this network and store it in a `.conf` file, the name of the file should be the same as that of the corresponding architecture (e.g. `VGG16.emadl` and `VGG16.conf`). For more information on architecture language please refer to [ConfLang project](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/languages/conflang) and the corresponding [SchemaLang](/monticore/EmbeddedMontiArc/languages/schemalang) for defining valid configuration combinations. An example of a training configuration:
```
configuration VGG16{
    num_epoch:10
    batch_size:64
    normalize:true
    load_checkpoint:false
    optimizer:adam{
        learning_rate:0.01
        learning_rate_decay:0.8
        step_size:1000
    }
}
```
 
### 3. a) Not Using the Maven Plugin
Generate C code which uses neural networks that were trained using the specified deep learning backend. The generator receives the following command line parameters:
    * `-m`    path to directory with EMADL models
    * `-r`    name of the root model
    * `-o`    output path
    * `-b`    backend
    * `-p`   path to python (Not mandatory; Default is `/usr/bin/python`)
    * `-f`   forced training (Not mandatory; values can be `y` for a forced training and `n` for a skip (a forced no-training)). By default, the hash value (from the training and test data, the structure of the model (.emadl) and the training parameters (.conf) of the model) will be compared. The model is retrained only if the hash changes. This can be used to distribute trained models, by distributing the corresponding `.training_hash` file as well, which will prevent a retraining
    * `-c`    compiling of generated c code (Not mandators; Default is `y`). Disable by setting to `n` when running on Windows, or on machines without `make` and `cmake` commands

    Assuming both the architecture definition `VGG16.emadl` and the corresponding training configuration `VGG16.conf` are located in a folder `models` and the target code should be generated in  a `target` folder using the `MXNet` backend, an example of a command is then:
    ```java -jar embedded-montiarc-emadl-generator-0.2.10-jar-with-dependencies.jar -m models -r VGG16 -o target -b MXNET```

### 3. b) Using the Maven Plugin
Generate C code which uses neural networks that were trained using the specified deep learning backend. 
The command line parameters are mapped to parameters that are specified in the POM.xml file:
    * `<backend>`  path to directory with EMADL models
    * `<modelToTrain>`    name of the root model
    * `<pathTmpOut>`    output path
    * `<backend>`    backend
    * `<pathToPython>`   path to python (Not mandatory; Default is `/usr/bin/python`)

### AdaNet related changes
There is a String variable within the EMADLGenerator "adaNetUtils" this variable holds the path to the AdaNet python files.
This has been added for possible future work. An ArchitectureSymbol has the same variable and is ,for now, hardcoded to the same 
value. Still it gets set by the EMADLGenerator. There is a Check in CNNArch that checks if the path has been changed. 
The check ```CheckAdaNetPathToFilesExists```

## Training using the Computer Cluster
If your models require a lot of time to process the training, consider using the [RWTH HPC](https://help.itc.rwth-aachen.de/service/rhr4fjjutttf/).

1. Create an account using the RWTH Selfservice identity management system. (1-2 days)


2. Use this account to access a dialog system of the cluster using ``ssh`` e.g.

   ```ssh -l <your_userid>:login18-g-1.hpc.itc.rwth-aachen.de```


3. Generate your training files and send them with the ``scp`` command to your file system on the cluster or clone a repository with git. (It doesn't matter which dialog system you use. You can access the same file system with any dialog system).
   
    ```scp -r <source_directory> <target_host>:<target_directory>```


4. Make sure that you have the necessary configurations in order to run your code. Create a batch script with SLURM commands in which you can configure the training environment like the used hardware, for example how many GPUs you want to use. You can read the documentation [here](https://help.itc.rwth-aachen.de/service/rhr4fjjutttf/article/13ace46cfbb84e92a64c1361e0e4c104/).
   

5. If you want to use GPUs, you need to load e.g. the pre-installed CUDA toolkit using ``module load cuda``. To list all loadable modules you can use ``module avail``. For more information on the module system click [here](https://help.itc.rwth-aachen.de/service/rhr4fjjutttf/article/417f822b8a7849eb8c9c2753045ad67f/).


6. You probably need libraries, that are not available in the module system. Create a virtual environment with python and install the libraries with ``pip`` e.g.

    ```
    python3 -m venv env      #<-- only once inorder to create the environment
    source env/bin/activate  #<-- use this when reconnecting to load the packages
    pip install mxnet-cu102
    ```

Now you can simply run the code (with your batch script containing the invocation of the virtual machine, loading of the modules and the call of the train routine) and send the trained model back to your machine using ``scp``.

Here is an example batch script.
    
```
#!/usr/local_rwth/bin/zsh
#SBATCH --gres=gpu:volta:2
#SBATCH --output=<your_home_directory>/slurmOutput.txt

module load cuda
module load cudnn
module load nccl

source <your_python_environement>/bin/activate

python3 example/CNNTrainer_<your_network_component>.py
```

You can submit the job with `sbatch <your-bash-script>.sh` on a backend node. It is also possible to just run the script on the frontend node.
If you did submit the job, you can access the status information with `sacct`.

## Tracking Experiments
The EMADL2CPP generator offers a tracking system capable of automatically forwarding all metadata generated during training to multiple tracking frameworks such as [MLflow](https://www.mlflow.org).

- A guide on how to use the tracking system, as an end user, is available [here](documents/experiment_tracking/using_the_tracking_system.md).
- A guide on how to add new tracking frameworks, as a developer extending the generator, is available [here](documents/experiment_tracking/implementing_new_tracking_backends.md).

# Presentations (Architecture & Hyperparameter Optimization)

For improvement of EmbeddedMontiArcDL models optimization methods for the architectures and hyperparameters are considered.

Different method for the hyperparameter optimization are presented here: [Hyperparameter_Optimization.pdf](documents/presentations/Hyperparameter_Optimization.pdf)

Different methods for neural architecture search and model evaluation methods are presented here:
[Neural_Architecture_Search.pdf](documents/presentations/Neural_Architecture_Search.pdf)
