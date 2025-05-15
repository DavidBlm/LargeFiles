/* (c) https://github.com/MontiCore/monticore */
package episodicMemorySimple;
conforms to dltag.DataPathTagSchema, dltag.LayerPathParameterTagSchema;

tags episodic {
tag Network with DataPath = {path = src/test/resources/training_data/episodicMemorySimple, type = HDF5};
tag Network with LayerPathParameter = {path = src/test/resources/pretrained/episodicMemorySimple, id = simple};
}
