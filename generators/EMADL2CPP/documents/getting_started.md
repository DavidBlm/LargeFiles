# Getting started
This guide is intended for developers wanting to work on the EMADL2CPP generator.
It provides the required information to set up the development environment and to run the generator with the PyTorch backend.
It assumes, that you already have Python and Java installed on your system, and use IntelliJ IDEA as your IDE.

Java 8 is required to run the generator. If you are on a UNIX system, you can use [SDKMAN](https://sdkman.io/) to install and manage multiple versions of Java on your system.
The guide has been tested with Python 3.10.2, but other versions might work as well.

## Installing python dependencies
It is recommended to use a virtual environment to manage the Python dependencies of the generator.
Instructions on how to set up a virtual environment can be found [here](https://docs.python.org/3/library/venv.html). Alternatively, you can use [Anaconda](https://www.anaconda.com/) to manage your Python environment.
Once inside the virtual environment, you can install the required dependencies using pip:
```bash
pip install --upgrade pip
pip install h5py torch dgl mlflow
```

## Setting up the development environment
1. Clone the repository
2. Open the project in IntelliJ IDEA
3. Click on `Maven` in the right-hand sidebar, then on the wrench icon and finally on `Maven Settings`
4. Check the `Override` checkbox next to `User settings file` and select the `settings.xml` file from the root of the repository
5. Click on `Apply` and `OK`
6. Click the `Reload All Maven Projects` button left of the wrench icon from step 3
7. Add a new run configuration (`Run` -> `Edit Configurations...` -> `+` -> `Application`) with the following settings:
   - Name: `MontiAnnaCli`
   - SDK: `java 8`
   - Main class: `de.monticore.lang.monticar.emadl.generator.MontiAnnaCli`
   - Program arguments: `-m src/main/resources/adanet_experiment/emadl -r mnist.MnistClassifier -o target -b PYTORCH`
8. Add another run configuration with the following settings:
   - Name: `AutoMLCli`
   - SDK: `java 8`
   - Main class: `de.monticore.lang.monticar.emadl.generator.AutoMLCli`
   - Program arguments: `-m src/main/resources/adanet_experiment/emadl -r mnist.MnistClassifier -o target -b PYTORCH`

If you are using a virtual environment, navigate to the PythonPipeline class (`de.monticore.mlpipelines.pipelines.PythonPipeline`) and change line 136 (`command.add("python3");`) to point to the Python executable in your virtual environment.
This is a temporary workaround because the `-p` option of the generator is currently not used. This should be fixed in the future. WARNING: Do not commit this change.

## Running the generator
With the run configurations set up, you can now run the generator in single-run (`MontiAnnaCli`) or AutoML (`AutoMLCli`) mode by selecting the respective run configuration and clicking the green arrow in the top right corner of IntelliJ IDEA.
If you want to use experiment tracking, all required information can be found [here](documents/experiment_tracking/using_the_tracking_system.md).