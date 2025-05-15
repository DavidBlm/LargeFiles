/* (c) https://github.com/MontiCore/monticore */
package sentimentanalyzer;
conforms to dltag.DataPathTagSchema, artifacttag.LayerArtifactParameterTagSchema, 
            dltag.LayerPathParameterTagSchema;

tags sentimentanalyzer {
tag Network with DataPath = {path = src/test/resources/training_data/sst2, type = HDF5};
tag Network with LayerArtifactParameter = {artifact = de.monticore.lang.monticar.pretrained:simple-pretrained:1, id = bert_small};
}
