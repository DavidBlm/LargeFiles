cmake_minimum_required(VERSION 3.5)
set(CMAKE_CXX_STANDARD 14)

project(mountaincar_master LANGUAGES CXX)

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
add_library(mountaincar_master mountaincar_master.cpp)
target_include_directories(mountaincar_master PUBLIC ${CMAKE_CURRENT_SOURCE_DIR} ${INCLUDE_DIRS})
target_link_libraries(mountaincar_master PUBLIC ${LIBS})
set_target_properties(mountaincar_master PROPERTIES LINKER_LANGUAGE CXX)

# export cmake project
export(TARGETS mountaincar_master FILE mountaincar_master.cmake)

# additional commands end
