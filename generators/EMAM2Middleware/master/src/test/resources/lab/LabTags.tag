/* (c) https://github.com/MontiCore/monticore */
package lab;
conforms to de.monticore.lang.monticar.generator.roscpp.RosToEmamTagSchema;

tags LabTags {
    //syntax: tag $port with RosConnection = {topic=($topicName,$topicType), msgField=$fieldOfTopicType};

    tag system.rosIn[1] with RosConnection = {topic=(/in1,std_msgs/Float64), msgField=data};
    tag system.rosIn[2] with RosConnection = {topic=(/in2,std_msgs/Float64), msgField=data};
    tag system.rosIn[3] with RosConnection = {topic=(/in3,std_msgs/Float64), msgField=data};
    tag system.rosIn[4] with RosConnection = {topic=(/in4,std_msgs/Float64), msgField=data};

    tag system.alex.input with RosConnection = {topic=(/in1,std_msgs/Float64), msgField=data};
    tag system.dinhAn.input with RosConnection = {topic=(/in2,std_msgs/Float64), msgField=data};
    tag system.philipp.input with RosConnection = {topic=(/in3,std_msgs/Float64), msgField=data};
    tag system.michael.input with RosConnection = {topic=(/in4,std_msgs/Float64), msgField=data};

    tag system.alex.out1 with RosConnection = {topic=(/combine_in1,std_msgs/Float64), msgField=data};
    tag system.dinhAn.out1 with RosConnection = {topic=(/combine_in2,std_msgs/Float64), msgField=data};
    tag system.philipp.out1 with RosConnection = {topic=(/combine_in3,std_msgs/Float64), msgField=data};
    tag system.michael.out1 with RosConnection = {topic=(/combine_in4,std_msgs/Float64), msgField=data};

    tag system.combine.in1 with RosConnection = {topic=(/combine_in1,std_msgs/Float64), msgField=data};
    tag system.combine.in2 with RosConnection ={topic=(/combine_in2,std_msgs/Float64), msgField=data};
    tag system.combine.in3 with RosConnection = {topic=(/combine_in3,std_msgs/Float64), msgField=data};
    tag system.combine.in4 with RosConnection = {topic=(/combine_in4,std_msgs/Float64), msgField=data};

    tag system.combine.out1 with RosConnection = {topic=(/combine_out,std_msgs/Float64), msgField=data};

    tag system.rosOut with RosConnection = {topic=(/combine_out,std_msgs/Float64), msgField=data};
}
