/* (c) https://github.com/MontiCore/monticore */
package cartpole;
conforms to de.monticore.lang.monticar.generator.roscpp.RosToEmamTagSchema;

tags Master {
    tag master.state with RosConnection = {topic=(/state, std_msgs/Float32MultiArray)};
    tag master.action with RosConnection = {topic=(/step, std_msgs/Int32)};
}
