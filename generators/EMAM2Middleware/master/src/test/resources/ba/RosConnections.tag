/* (c) https://github.com/MontiCore/monticore */
package ba;
conforms to de.monticore.lang.monticar.generator.roscpp.RosToEmamTagSchema;

tags{
    //system
    tag system.cutoffTimeIn with RosConnection;
    tag system.isActiveIn with RosConnection;
    tag system.absTrajectory[1:2] with RosConnection;
    tag system.hullsIn[1:2] with RosConnection;
    tag system.collisionOut with RosConnection;
    tag system.maxAccel[1:2] with RosConnection;
    tag system.maxVel[1:2] with RosConnection;
    tag system.deltaTime[1:2] with RosConnection;
    tag system.holdTime[1:2] with RosConnection;
    tag system.resetVel[1:2] with RosConnection;
    tag system.slowDown[1:2] with RosConnection;
    tag system.curVel[1:2] with RosConnection;

    //intersectionController
    tag system.intersectionController.trajectoryIn[1:2] with RosConnection;
    tag system.intersectionController.cutoffTime with RosConnection;
    tag system.intersectionController.isActive with RosConnection;

    tag system.intersectionController.stop[1] with RosConnection =
        {topic = (/sim/comm/slowDown1, std_msgs/Bool), msgField = data};
    tag system.intersectionController.stop[2] with RosConnection =
        {topic = (/sim/comm/slowDown2, std_msgs/Bool), msgField = data};


    //collisionDetection
    tag system.collisionDetection.hulls[1:2] with RosConnection;
    tag system.collisionDetection.collision with RosConnection;

    //velocityController
    tag system.velocityController[1:2].maxVelIn with RosConnection;
    tag system.velocityController[1:2].maxAccelIn with RosConnection;
    tag system.velocityController[1:2].deltaTimeIn with RosConnection;
    tag system.velocityController[1:2].holdTimeIn with RosConnection;
    tag system.velocityController[1:2].resetVelIn with RosConnection;
    tag system.velocityController[1:2].curVelOut with RosConnection;

    tag system.velocityController[1].slowDownIn with RosConnection = {topic = (/v1/comm/in/slowDown1, std_msgs/Bool), msgField = data};
    tag system.velocityController[2].slowDownIn with RosConnection = {topic = (/v2/comm/in/slowDown2, std_msgs/Bool), msgField = data};

    //stopCommQuality
    tag system.stopCommQuality[1].in1 with RosConnection = {topic = (/sim/comm/slowDown1, std_msgs/Bool), msgField = data};
    tag system.stopCommQuality[2].in1 with RosConnection = {topic = (/sim/comm/slowDown2, std_msgs/Bool), msgField = data};

    tag system.stopCommQuality[1].out1 with RosConnection = {topic = (/v1/comm/in/slowDown1, std_msgs/Bool), msgField = data};
    tag system.stopCommQuality[2].out1 with RosConnection = {topic = (/v2/comm/in/slowDown2, std_msgs/Bool), msgField = data};

}
