/* (c) https://github.com/MontiCore/monticore */
package tests.matrix;
conforms to de.monticore.lang.monticar.generator.roscpp.RosToEmamTagSchema;

tags ThreeDimMatrixComp{
    tag threeDimMatrixComp.in1 with RosConnection = {topic=(/topicName1,std_msgs/Float64MultiArray)};
    tag threeDimMatrixComp.in2 with RosConnection = {topic=(/topicName2,std_msgs/Int32MultiArray)};
    tag threeDimMatrixComp.in3 with RosConnection = {topic=(/topicName3,std_msgs/ByteMultiArray)};
    tag threeDimMatrixComp.out1 with RosConnection = {topic=(/topicName4,std_msgs/Float64MultiArray)};
    tag threeDimMatrixComp.out2 with RosConnection = {topic=(/topicName5,std_msgs/Int32MultiArray)};
    tag threeDimMatrixComp.out3 with RosConnection = {topic=(/topicName6,std_msgs/ByteMultiArray)};
}
