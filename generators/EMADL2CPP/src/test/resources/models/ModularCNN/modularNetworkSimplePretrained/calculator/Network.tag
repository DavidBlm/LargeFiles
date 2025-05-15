/* (c) https://github.com/MontiCore/monticore */
package calculator;
conforms to artifacttag.DatasetArtifactTagSchema, artifacttag.LayerArtifactParameterTagSchema, 
            dltag.LayerPathParameterTagSchema, dltag.DataPathTagSchema;

tags NeuronalNetwork {
tag Network with DataPath = {path = src/test/resources/models/ModularCNN/training_data/dummy, type = HDF5};
 tag Network with LayerPathParameter = {path = src/test/resources/models/ModularCNN/modularNetworkSimplePretrained/resources/pretrainedNet1, id = mnist_pre};
}

