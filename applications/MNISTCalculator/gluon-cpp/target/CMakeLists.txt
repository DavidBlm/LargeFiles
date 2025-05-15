cmake_minimum_required(VERSION 3.5)
set(CMAKE_CXX_STANDARD 14)

project(cNNCalculator_connector LANGUAGES CXX)

#set cmake module path
set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} ${CMAKE_CURRENT_SOURCE_DIR}/cmake)

# add dependencies
find_package(Armadillo REQUIRED)
set(INCLUDE_DIRS ${INCLUDE_DIRS} ${Armadillo_INCLUDE_DIRS})
set(LIBS ${LIBS} ${Armadillo_LIBRARIES})


# additional library linkage
set(LIBS ${LIBS} mxnet)

# additional commands

# create static library
include_directories(${INCLUDE_DIRS})
add_library(cNNCalculator_connector cNNCalculator_connector.cpp)
target_include_directories(cNNCalculator_connector PUBLIC ${CMAKE_CURRENT_SOURCE_DIR} ${INCLUDE_DIRS})
target_link_libraries(cNNCalculator_connector PUBLIC ${LIBS})
set_target_properties(cNNCalculator_connector PROPERTIES LINKER_LANGUAGE CXX)

# export cmake project
export(TARGETS cNNCalculator_connector FILE cNNCalculator_connector.cmake)

# additional commands end
