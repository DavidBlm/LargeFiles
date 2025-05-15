/* (c) https://github.com/MontiCore/monticore */
package tests.mqtt;
conforms to de.monticore.lang.embeddedmontiarc.tagging.middleware.mqtt.MqttToEmamTagSchema;

tags Test {
tag testComp.in1 with MqttConnection = {topic = /clockQ};
tag testComp.in2 with MqttConnection = {topic = /clockN};
tag testComp.in3 with MqttConnection = {topic = /clockZ};
tag testComp.in4 with MqttConnection = {topic = /clockB};
tag testComp.out1 with MqttConnection = {topic = /clockQ};
tag testComp.out2 with MqttConnection = {topic = /clockN};
tag testComp.out3 with MqttConnection = {topic = /clockZ};
tag testComp.out4 with MqttConnection = {topic = /clockB};
}
