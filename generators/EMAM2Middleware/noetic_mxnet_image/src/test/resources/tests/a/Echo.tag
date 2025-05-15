/* (c) https://github.com/MontiCore/monticore */
package tests.a;
conforms to de.monticore.lang.monticar.generator.roscpp.RosToEmamTagSchema;

tags Echo {
tag compA.rosIn with RosConnection = {topic = (/clock, rosgraph_msgs/Clock), msgField = clock.toSec()};
tag compA.rosOut with RosConnection = {topic = (/echo, std_msgs/Float64), msgField = data};
}
