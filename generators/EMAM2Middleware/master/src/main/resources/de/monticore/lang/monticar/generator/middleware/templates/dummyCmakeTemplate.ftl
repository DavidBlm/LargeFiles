<#-- (c) https://github.com/MontiCore/monticore -->
cmake_minimum_required(VERSION 3.5)
project (DummyAdapter_${compName})

add_library(DummyAdapter_${compName} DummyAdapter_${compName}.cpp)
set_target_properties(DummyAdapter_${compName} PROPERTIES LINKER_LANGUAGE CXX)
target_link_libraries(DummyAdapter_${compName} ${compName} IAdapter_${compName})
target_include_directories(DummyAdapter_${compName} PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})
export(TARGETS DummyAdapter_${compName} FILE DummyAdapter_${compName}.cmake)
