cmake_minimum_required(VERSION 3.5)
set(CMAKE_CXX_STANDARD 14)

project(add LANGUAGES CXX)

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
add_library(add add.cpp)
target_include_directories(add PUBLIC ${CMAKE_CURRENT_SOURCE_DIR} ${INCLUDE_DIRS})
target_link_libraries(add PUBLIC ${LIBS})
set_target_properties(add PROPERTIES LINKER_LANGUAGE CXX)

# export cmake project
export(TARGETS add FILE add.cmake)

# additional commands end
