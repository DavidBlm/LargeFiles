# Using the tracking system
## Configuration
The tracking system is simultaneously enabled and configured by providing the path to a valid configuration file (written in the ConfLang language) as a CLI parameter (`-track`). Since the name of the configuration currently has no impact on the workings of the tracking system, it can be chosen arbitrarily. The configuration file must contain one entry for each tracking backend that should be enabled. The key of these entries must be in lower case and match the name of an integrated tracking backend as defined in the enum [TrackingBackend](src/main/java/de/monticore/mlpipelines/tracking/TrackingBackend.java). The semantics of the associated value is not predefined and may differ depending on the tracking backend. In the case of the [MLflowTracker](src/main/java/de/monticore/mlpipelines/tracking/tracker/MLflowTracker.java) and [StdOutTracker](src/main/java/de/monticore/mlpipelines/tracking/tracker/StdOutTracker.java), the string value specifies the name of the current experiment used to group runs.

Backend-specific settings can be applied by extending the respective configuration entry to an nested configuration entry. This nested entry may then hold arbitrary subentries defining the behavior of the tracking backend. See [Integrated Backends](#integrated_backends) for detailed information on which settings are available for each integrated tracking backend. Besides configuration entries enabling corresponding backends, the configuration file may be used to set global options modifying the behavior of all active tracking backends. By including the `tags` configuration entry, custom tags can be automatically logged for every experiment run. The value of this entry must be a list of lists, with the latter containing exactly two strings. The first string represents the name of the tag, and the second string its value. Finally, a configuration entry named `param_blacklist` may be set to specify parameters that should not be logged. Its value must be a list of strings, representing simple regular expressions. These simple regular expressions only feature the asterisk (`*`) as a wildcard character, with all other characters being handled as literals. This allows to efficiently blacklist all subentries of nested hyperparameters. An example tracking configuration file is available [here](src/main/resources/experiments/tracking/trackingConfigurationExample.conf).

## Integrated Backends
### MLflow
[MLflow](https://www.mlflow.org) is an open-source suite of tools engineered to streamline the machine learning workflow by assisting practictioners across the different stages of ML model development. The corresponding implementation of the RunTracker interface is available [here](src/main/java/de/monticore/mlpipelines/tracking/tracker/MLflowTracker.java).

#### Tracking Server
In order to host an MLflow tracking server, the `mlflow` Python package has to be installed on the corresponding system. It is recommended to choose the same version used by the EMADL2CPP generator. The server can then be started with the `mlflow server` command. By default, all metadata except for artifacts are saved into the local directory `./mlruns`. This behavior can be overwritten with the `--backend-store-uri` option, which takes a database connection string or a local filesystem URI. Similarly, artifacts are saved to the `./mlartifacts` directory, unless the `--artifacts-destination` option has been specified. MLflow can be configured to store artifacts on a variety of storage backends, including Amazon S3, NFS, and custom local directories.

#### Configuration
The value of the configuration entry enabling the MLflow tracking backend must be a string, representing the name of the current experiment. A corresponding experiment will be created on the tracking server (if it does not exist yet), grouping all subsequent runs. Backend-specific settings:

| Key | Value Type | Description | Required |
|---|---|---|:---:|
| `tracking_uri` | String | URI of the tracking server, e.g. `"http://localhost:5000"` | **Yes** |
| `clean_up` | String / List of Strings | States of runs to be deleted after training completion. Useful to automatically remove failed or canceled runs.<br>Valid values: `RUNNING`, `SCHEDULED`, `FINISHED`, `FAILED` and `KILLED`   | No |

#### Caveats
1. It is required to install the `mlflow` Python package on the local system, even if the tracking server is hosted remotely. This is because of a technical limitation of the MLflow Java client, which wraps the Python package for certain functionalities. If this package is not installed globally, the environment variable `MLFLOW_PYTHON_EXECUTABLE`, pointing to the virtual Python environment where MLflow is installed, must be set prior to running the generator.
2. When using the optional `clean_up` setting, the state `RUNNING` should only be included when not working collaboratively on the same tracking server and experiment, as this could result in the loss of runs currently executed by other users.

### StdOut
The StdOutTracker is a simple dummy tracker forwarding all tracking instructions to the console. It's purpose is mostly for debugging, serving as a simple reference for future tracking backends and showcasing the tracking system's ability to manage multiple tracking backends at once. The corresponding implementation of the RunTracker interface is available [here](src/main/java/de/monticore/mlpipelines/tracking/tracker/StdOutTracker.java).

#### Tracking Server
Since all tracking instructions are sent to the console, no remote tracking server has to be hosted.

#### Configuration
The value of the configuration entry enabling the StdOut tracking backend must be a string, representing the name of the current experiment. Backend-specific settings: *None*