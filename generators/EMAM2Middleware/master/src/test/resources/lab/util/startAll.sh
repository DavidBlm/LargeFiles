#!/bin/bash
# (c) https://github.com/MontiCore/monticore  

# Kill all spawned subshells on exit
trap 'kill $(jobs -p)' EXIT

cd target/generated-sources-cmake/lab/
cd build

./lab_system_alex/coordinator/Coordinator_lab_system_alex -t 1000 &
./lab_system_dinhAn/coordinator/Coordinator_lab_system_dinhAn -t 1000 &
./lab_system_michael/coordinator/Coordinator_lab_system_michael -t 1000 &
./lab_system_philipp/coordinator/Coordinator_lab_system_philipp -t 1000 &
./lab_system_combine/coordinator/Coordinator_lab_system_combine -t 1000
