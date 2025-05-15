/* (c) https://github.com/MontiCore/monticore */
package tests.emadlTests;
conforms to de.monticore.lang.monticar.generator.roscpp.RosToEmamTagSchema;

tags ResNet34 {
    tag resNet34.image with RosConnection = {topic=(/image, std_msgs/Int32MultiArray)};
    tag resNet34.predictions with RosConnection = {topic=(/predictions, std_msgs/Float64MultiArray)};
}
