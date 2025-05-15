/* (c) https://github.com/MontiCore/monticore */
package tagging;
conforms to dltag.DataPathTagSchema;

tags Alexnet {
tag Alexnet with DataPath = {path = src/test/resources/models, type = LMDB};
tag AlexnetInvalid with DataPath = {path = test/resources/models, type = random};
tag AlexnetInvalidType with DataPath = {path = src/test/resources/models, type = LMBD};
tag Parent.a1 with DataPath = {path = src/test/resources/models, type = LMDB};
tag Parent.a2 with DataPath = {path = src/test/resources/models, type = LMDB};
tag ParentInvalidType.a1 with DataPath = {path = src/test/resources/models, type = LMDB};
tag ParentInvalidType.a2 with DataPath = {path = src/test/resources/models, type = LMDB_1};
tag ParentInvalidPath.a1 with DataPath = {path = /NO_VALID_PATH/, type = LMDB};
tag ParentInvalidPath.a2 with DataPath = {path = src/test/resources/models, type = LMDB};
}



