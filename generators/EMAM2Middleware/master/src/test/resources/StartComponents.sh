#!/usr/bin/env bash
# (c) https://github.com/MontiCore/monticore  
#starts all 4 components

targetFolder=target/generated-sources-cmake/system/build
sleepTime=100

(${targetFolder}/ba_system_collisionDetection/coordinator/Coordinator_ba_system_collisionDetection -t $sleepTime) & \
(${targetFolder}/ba_system_intersectionController/coordinator/Coordinator_ba_system_intersectionController -t $sleepTime) & \
(${targetFolder}/ba_system_velocityController_1_/coordinator/Coordinator_ba_system_velocityController_1_ -t $sleepTime) & \
(${targetFolder}/ba_system_velocityController_2_/coordinator/Coordinator_ba_system_velocityController_2_ -t $sleepTime)
