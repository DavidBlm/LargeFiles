cmake_minimum_required(VERSION 3.5)
set(CMAKE_CXX_STANDARD 14)

project(cartpole_master LANGUAGES CXX)

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
add_library(cartpole_master cartpole_master.cpp)
target_include_directories(cartpole_master PUBLIC ${CMAKE_CURRENT_SOURCE_DIR} ${INCLUDE_DIRS})
target_link_libraries(cartpole_master PUBLIC ${LIBS})
set_target_properties(cartpole_master PROPERTIES LINKER_LANGUAGE CXX)

# export cmake project
export(TARGETS cartpole_master FILE cartpole_master.cmake)

# additional commands end
