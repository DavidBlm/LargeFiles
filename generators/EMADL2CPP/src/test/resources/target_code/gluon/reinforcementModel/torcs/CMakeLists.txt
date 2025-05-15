cmake_minimum_required(VERSION 3.5)
set(CMAKE_CXX_STANDARD 14)

project(torcs_agent_torcsAgent LANGUAGES CXX)

#set cmake module path
set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} ${CMAKE_CURRENT_SOURCE_DIR}/cmake)

# add dependencies
find_package(Armadillo REQUIRED)
set(INCLUDE_DIRS ${INCLUDE_DIRS} ${Armadillo_INCLUDE_DIRS})
set(LIBS ${LIBS} ${Armadillo_LIBRARIES})


# additional library linkage

# additional commands

# create static library
include_directories(${INCLUDE_DIRS})
add_library(torcs_agent_torcsAgent torcs_agent_torcsAgent.cpp)
target_include_directories(torcs_agent_torcsAgent PUBLIC ${CMAKE_CURRENT_SOURCE_DIR} ${INCLUDE_DIRS})
target_link_libraries(torcs_agent_torcsAgent PUBLIC ${LIBS})
set_target_properties(torcs_agent_torcsAgent PROPERTIES LINKER_LANGUAGE CXX)

# export cmake project
export(TARGETS torcs_agent_torcsAgent FILE torcs_agent_torcsAgent.cmake)

# additional commands end
