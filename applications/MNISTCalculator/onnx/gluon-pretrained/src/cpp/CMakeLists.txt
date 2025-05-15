cmake_minimum_required(VERSION 3.5)
project(DigitCalculator CXX)
set(CMAKE_CXX_STANDARD 11)

option(MXNET_PATH "Path to libmxnet.so")


add_executable(DigitCalculator DigitCalculator.cpp)

set_target_properties(DigitCalculator PROPERTIES LINKER_LANGUAGE CXX)
find_package(OpenCV REQUIRED)
target_link_libraries(DigitCalculator ${MXNET_PATH})
set(INCLUDE_DIRS ${INCLUDE_DIRS} ${OpenCV_INCLUDE_DIRS})
set(LIBS ${LIBS} ${OpenCV_LIBS})
target_link_libraries(DigitCalculator  cNNCalculator_connector ${LIBS} -lblas -lopenblas)
target_include_directories(DigitCalculator PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})
export(TARGETS DigitCalculator FILE DigitCalculator.cmake)
