/* (c) https://github.com/MontiCore/monticore */
package tests.matrix;
conforms to de.monticore.lang.monticar.generator.roscpp.RosToEmamTagSchema;

tags TwoDimMatrixComp{
    tag twoDimMatrixComp.in1 with RosConnection = {topic=(/topicName1,std_msgs/Float64MultiArray)};
    tag twoDimMatrixComp.in2 with RosConnection = {topic=(/topicName2,std_msgs/Int32MultiArray)};
    //tag twoDimMatrixComp.in3 with RosConnection = {topic=(/topicName3,std_msgs/ByteMultiArray)};
    tag twoDimMatrixComp.out1 with RosConnection = {topic=(/topicName4,std_msgs/Float64MultiArray)};
    tag twoDimMatrixComp.out2 with RosConnection = {topic=(/topicName5,std_msgs/Int32MultiArray)};
    //tag twoDimMatrixComp.out3 with RosConnection = {topic=(/topicName6,std_msgs/ByteMultiArray)};
}
