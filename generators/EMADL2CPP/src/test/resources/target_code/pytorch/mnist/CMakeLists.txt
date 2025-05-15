cmake_minimum_required(VERSION 3.5)
set(CMAKE_CXX_STANDARD 14)

project(mnist_mnistClassifier LANGUAGES CXX)

#set cmake module path
set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} ${CMAKE_CURRENT_SOURCE_DIR}/cmake)

# add dependencies
find_package(Armadillo REQUIRED)
set(INCLUDE_DIRS ${INCLUDE_DIRS} ${Armadillo_INCLUDE_DIRS})
set(LIBS ${LIBS} ${Armadillo_LIBRARIES})


# additional library linkage

# additional commands
find_package (OpenCV 4.0.0 REQUIRED)
include_directories (${OpenCV_INCLUDE_DIRS})
find_package(Torch REQUIRED)
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${TORCH_CXX_FLAGS}")
set(TORCH_INCLUDE_DIRS ${TORCH_INSTALL_PREFIX}/include ${TORCH_INSTALL_PREFIX}/include/torch/csrc/api/include)

# create static library
include_directories(${INCLUDE_DIRS})
add_library(mnist_mnistClassifier mnist_mnistClassifier.cpp)
target_include_directories(mnist_mnistClassifier PUBLIC ${CMAKE_CURRENT_SOURCE_DIR} ${INCLUDE_DIRS})
target_link_libraries(mnist_mnistClassifier PUBLIC ${LIBS})
set_target_properties(mnist_mnistClassifier PROPERTIES LINKER_LANGUAGE CXX)

# export cmake project
export(TARGETS mnist_mnistClassifier FILE mnist_mnistClassifier.cmake)

# additional commands end
