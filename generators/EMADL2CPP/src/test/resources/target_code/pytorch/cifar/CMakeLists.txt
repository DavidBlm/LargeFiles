cmake_minimum_required(VERSION 3.5)
set(CMAKE_CXX_STANDARD 14)

project(cifar10_cifar10Classifier LANGUAGES CXX)

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
add_library(cifar10_cifar10Classifier cifar10_cifar10Classifier.cpp)
target_include_directories(cifar10_cifar10Classifier PUBLIC ${CMAKE_CURRENT_SOURCE_DIR} ${INCLUDE_DIRS})
target_link_libraries(cifar10_cifar10Classifier PUBLIC ${LIBS})
set_target_properties(cifar10_cifar10Classifier PROPERTIES LINKER_LANGUAGE CXX)

# export cmake project
export(TARGETS cifar10_cifar10Classifier FILE cifar10_cifar10Classifier.cmake)

# additional commands end
