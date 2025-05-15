/* (c) https://github.com/MontiCore/monticore */
package tests.dist;
conforms to de.monticore.lang.monticar.generator.roscpp.RosToEmamTagSchema;

tags Cluster{
tag twoCompCluster.sub1.rosOut with RosConnection;
tag twoCompCluster.sub3.rosIn with RosConnection;

tag invalidSuperConnection.sub1.rosIn with RosConnection;

tag validSuperConnection.rosIn with RosConnection;
tag validSuperConnection.rosOut with RosConnection;
tag validSuperConnection.sub1.rosIn with RosConnection;
tag validSuperConnection.sub1.rosOut with RosConnection;

}
