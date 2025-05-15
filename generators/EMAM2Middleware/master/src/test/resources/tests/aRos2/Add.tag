/* (c) https://github.com/MontiCore/monticore */
package tests.aRos2;
conforms to de.monticore.lang.monticar.generator.roscpp.RosToEmamTagSchema;

tags Add {
    tag addComp.in1 with RosConnection = {topic = (/clock, std_msgs/msg/Float64), msgField = data};
    tag addComp.out1 with RosConnection = {topic = (/echo, std_msgs/msg/Float64), msgField = data};
}
