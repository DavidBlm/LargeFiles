/* (c) https://github.com/MontiCore/monticore */
package tests.tagging;
conforms to de.monticore.lang.monticar.generator.roscpp.RosToEmamTagSchema;

tags OptionalMsgField{
tag optionalMsgField.in1 with RosConnection = {topic = (name1, package/topic1)};
tag optionalMsgField.out1 with RosConnection = {topic = (name1, package/topic1) , msgField = msgField1};
}
