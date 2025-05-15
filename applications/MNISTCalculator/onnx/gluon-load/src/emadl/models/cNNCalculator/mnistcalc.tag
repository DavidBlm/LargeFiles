/* (c) https://github.com/MontiCore/monticore */
package cNNCalculator;
conforms to dltag.DataPathTagSchema, artifacttag.LayerArtifactParameterTagSchema, 
            dltag.LayerPathParameterTagSchema;

tags mnistcalculator {
tag Network with DataPath = {path = resources/training_data, type = HDF5};
tag Network with LayerArtifactParameter = {artifact = de.monticore.lang.monticar.pretrained:mnistcalc-tensorflow:1, id = mnistcalc};
}
