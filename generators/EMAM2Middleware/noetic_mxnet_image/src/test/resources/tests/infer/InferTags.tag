/* (c) https://github.com/MontiCore/monticore */
package tests.infer;
conforms to de.monticore.lang.monticar.generator.roscpp.RosToEmamTagSchema;

tags InferTags{
tag inferComp.rosIn with RosConnection = {topic = (/topic1, std_msgs/Float32), msgField = data};
tag inferComp.rosOut with RosConnection = {topic = (/topic2, std_msgs/Float32)};
tag inferComp.sub1.rosIn with RosConnection;
tag inferComp.sub1.rosOut with RosConnection;
}
