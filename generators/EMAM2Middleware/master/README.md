<!-- (c) https://github.com/MontiCore/monticore -->
# EMAM2Middleware
![pipeline](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/EMAM2Middleware/badges/master/build.svg)
![coverage](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/EMAM2Middleware/badges/master/coverage.svg)
## Purpose
This generator takes an EMAM or EMADL model and connects it to a middleware library. If all Ports of two connected Components are marked as middleware Ports, the generator will create 2 executables that can be deployed on different machines.
All communication of these 2 Components will then be tunneled trough the specified middleware:
![MiddlewareAdapter](/uploads/6e9c69e6b56554579551769174df3697/MiddlewareAdapter.png)
It also supports automatic clustering of the subcomponents to deploy on different machines.

## Other important documents
### Quickstart
If you want to use the generator for your project, check out [QUICKSTART_USER.md](QUICKSTART_USER.md).

If you want to add features to this generator, check out [QUICKSTART_DEVELOPER.md](QUICKSTART_DEVELOPER.md)

### Writing your own Middleware Generator
see [TUTORIAL_ADD_MIDDLEWARE.md](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/EMAM2Middleware/blob/master/TUTORIAL_ADD_MIDDLEWARE.md)

### Dependencies needed to compile the generated projects
See [INSTALL_DEPENDENCIES.md](INSTALL_DEPENDENCIES.md)

## Usage
### CLI
Maven generates the jar `embedded-montiarc-math-middleware-generator-{Version}-jar-with-dependencies.jar`
and the cli is located in `de.monticore.lang.monticar.generator.middleware.cli.DistributedTargetGeneratorCli`.

Parameters: `${file path to config json}` OR `-r ${raw json config string}`

Example: [CliUsage.sh](src/test/resources/CliUsage.sh)

An example config file with all clustering algorithms: [config](src/test/resources/config/parameterTest/clusterParamsAllAlgos.json)

| Name                 | Type   | Required | Description                                                                               |
|----------------------|--------|----------|-------------------------------------------------------------------------------------------|
| modelsDir            | String |     ✅    | path to directory with EMAM models                                                        |
| outputDir            | String |     ✅    | path to output directory for generated files                                              |
| rootModel            | String |     ✅    | fully qualified name of the root model                                                    |
| generators           | List   |     ✅    | List of generator identfiers<br> 'cpp', 'emadlcpp', 'roscpp', 'rclcpp'                    |
| emadlBackend         | String |     ❓    | deep-learning-framework backend<br> 'MXNET'(Default), 'CAFFE2', 'GLUON'                   |
| writeTagFile         | Bool   |     ❓    | Writes a .tag file with all Middleware tags into the generated code<br> Defaults to false |
| clusteringParameters | Object |     ❓    | Options to cluster the component before generating<br> See below                          |

Clustering Parameters:

| Name                | Type         | Required | Description                                                                                                                                                                                                                                       |
|---------------------|--------------|----------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| numberOfClusters    | int          | ❓        | Number of clusters the subcomponents should be divided into<br> Overrides numberOfClusters in algorithmParameters                                                                                                                                 |
| flatten             | bool         | ❓        | Replace all components with their subcomponents execpt when it is atomic or the flatten level is reached                                                                                                                                          |
| flattenLevel        | int          | ❓        | Maximal level of component flattening                                                                                                                                                                                                             |
| metric            | String       | ❓        | Metric to evaluate the quality of the resulting clusters. Available: "CommunicationCost"(Default), "Silhouette"|
| chooseBy            | String       | ❓        | Strategy to choose from the resulting clusterings<br> bestWithFittingN(Default): if numberOfClusters is set, all results with a different number of clusters are ignored<br> bestOverall: ignore numberOfClusters, choose result with best score |
| algorithmParameters | List<Object> | ❓        | Used to specify which algorithms(and their parameters) are used for clustering                                                                                                                                                                    |

There are 4 different Clustering Algorithms with distinct parameters

Every parameter of the clustering algorithms can be dynamic, enabling automatic search for the best values. Available are lists and generators as seen in the example below:
```json
"sigma":[1,2,3]
"sigma":{
  "min":1,
  "max":3,
  "step":1
}
"sigma":{
  "min":1,
  "max":3,
  "count":3
}
```
Also see [clusterDynamic.json](src/test/resources/config/parameterTest/clusterDynamic.json) and [clusterDynamicList.json](src/test/resources/config/parameterTest/clusterDynamicList.json)

Spectral Clustering:

| Name             | Type   | Required | Description                                                                     |
|------------------|--------|----------|---------------------------------------------------------------------------------|
| name             | String | ✅️        | must equal "SpectralClustering"                                                 |
| numberOfClusters | int    | ✅️        | Number of clusters that are created<br> Overwritten by global numberOfClusters  |
| l                | int    | ❓        |                                                                                 |
| sigma            | double | ❓        |                                                                                 |

DBScan:

| Name    | Type   | Required | Description         |
|---------|--------|----------|---------------------|
| name    | String | ✔️        | must equal "DBScan" |
| min_pts | int    | ✔️        |                     |
| radius  | double | ✔️        |                     |

Markov:

| Name         | Type   | Required | Description         |
|--------------|--------|----------|---------------------|
| name         | String | ✔️     | must equal "Markov" |
| max_residual | double | ❓        |                     |
| gamma_exp    | double | ❓        |                     |
| loop_gain    | double | ❓        |                     |
| zero_max     | double | ❓        |                     |

Affinity Propagation:

| Name | Type   | Required | Description                      |
|------|--------|----------|----------------------------------|
| name | String | ✔️        | must equal "AffinityPropagation" |


### Visulization of clustering results
There are 3 scripts available to visualise the results of the clustering process. They all create graphs for each of the 4 evaluation models:
1. [evaluationVisualisation.py](src/test/resources/evaluationVisualisation.py): bar graphs that compare the size of clusters, distance score, and time taken in ms
2. [montecarlovisualisation.py](src/test/resources/montecarlovisualisation.py): line graph visualising the average distance cost for random clustering(with Monte Carlo)
3. [silhouetteVisualisation.py](src/test/resources/silhouetteVisualisation.py): point graph visualising the silhouette score of different clusterings sorted by cluster size

Before using them install Python 3+ and the packages `matplotlib` and `numpy`.

After running `EvaluationTest`(Warning: very long runtime) you can visualise the results by calling(from the project root):
```bash
python3 src/test/resources/evaluationVisualisation.py target/evaluation/autopilot/emam/clusteringResults.json target/evaluation/pacman/emam/clusteringResults.json target/evaluation/supermario/emam/clusteringResults.json target/evaluation/daimler/emam/clusteringResults.json
```
or
```bash
python3 src/test/resources/montecarlovisualisation.py target/evaluation/autopilotMC/monteCarloResults.json target/evaluation/pacmanMC/monteCarloResults.json target/evaluation/supermarioMC/monteCarloResults.json target/evaluation/daimlerMC/monteCarloResults.json
```
or
```bash
python3 src/test/resources/silhouetteVisualisation.py target/evaluation/autopilotSilhouette/emam/clusteringResults.json target/evaluation/pacmanSilhouette/emam/clusteringResults.json target/evaluation/supermarioSilhouette/emam/clusteringResults.json target/evaluation/daimlerSilhouette/emam/clusteringResults.json
```

## Defining the connection between a component and the middleware
The connection between middleware and the component is defined as tags on Ports in .tag files.
### Example with ROS Middleware:
Tags of the type RosConnection can either be simple tags(see Example 3) or define a topic(http://wiki.ros.org/Topics) with name, type and optional msgField(http://wiki.ros.org/msg , 2.)
Examples:
1. [src/test/resources/tests/a/Add.tag](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/EMAM2Middleware/blob/master/src/test/resources/tests/a/Add.tag)
1. [src/test/resources/tests/a/Echo.tag](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/EMAM2Middleware/blob/master/src/test/resources/tests/a/Echo.tag)
1. [src/test/resources/tests/dist/SimpleDist.tag](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/EMAM2Middleware/blob/master/src/test/resources/tests/dist/SimpleDist.tag)

#### Use-case 1: Creating 1 executable
Look at [GenerationTest::testMiddlewareGenerator](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/EMAM2Middleware/blob/master/src/test/java/de/monticore/lang/monticar/generator/middleware/GenerationTest.java). The component is defined in [src/test/resources/tests/a/AddComp.emam](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/EMAM2Middleware/blob/master/src/test/resources/tests/a/AddComp.emam) and the tags for the connection to ros are defined in [src/test/resources/tests/a/Add.tag](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/EMAM2Middleware/blob/master/src/test/resources/tests/a/Add.tag)

#### Use-case 2: Creating multiple executables for distributed systems
Look at [GenerationTest::testDistributedTargetGenerator](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/EMAM2Middleware/blob/master/src/test/java/de/monticore/lang/monticar/generator/middleware/GenerationTest.java). The component is defined in [src/test/resources/dist/DistComp.emam](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/EMAM2Middleware/blob/master/src/test/resources/tests/dist/DistComp.emam) and the tags for the connection to ros are defined in [src/test/resources/tests/dist/SimpleDist.tag](https://git.rwth-aachen.de/monticore/EmbeddedMontiArc/generators/EMAM2Middleware/blob/master/src/test/resources/tests/dist/SimpleDist.tag)

## Running the Integration tests locally
To run the integration tests locally, `docker` needs to be installed. Install instructions can be found [here](https://docs.docker.com/install/).

Run the tests by executing [dockerLocalIntegrationTestRos.sh](src/test/bash/dockerLocalIntegrationTestRos.sh) or [dockerLocalIntegrationTestRos2.sh](src/test/bash/dockerLocalIntegrationTestRos2.sh) as root:
```bash
sudo src/test/bash/dockerLocalIntegrationTestRos.sh
sudo src/test/bash/dockerLocalIntegrationTestRos2.sh
```
