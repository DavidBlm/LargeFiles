/* (c) https://github.com/MontiCore/monticore */
package torcs.agent;
conforms to de.monticore.lang.monticar.generator.roscpp.RosToEmamTagSchema;

tags TorcsAgent {
    tag torcsAgent.state with RosConnection = {topic=(/torcs/state, std_msgs/Float32MultiArray)};
    tag torcsAgent.action with RosConnection = {topic=(/torcs/step, std_msgs/Float32MultiArray)};
}
