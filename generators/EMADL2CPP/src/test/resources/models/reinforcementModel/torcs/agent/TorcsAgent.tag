/* (c) https://github.com/MontiCore/monticore */
package torcs.agent;
conforms to de.monticore.lang.monticar.generator.roscpp.RosToEmamTagSchema;

tags TorcsAgent {
    tag torcsAgent.state with RosConnection = {topic=(preprocessor_state, std_msgs/Float32MultiArray)};
    tag torcsAgent.action with RosConnection = {topic=(postprocessor_action, std_msgs/Int32)};
}
