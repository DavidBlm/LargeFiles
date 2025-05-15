<#-- (c) https://github.com/MontiCore/monticore -->
cmake_minimum_required(VERSION 3.5)
project (Coordinator_${compName} LANGUAGES CXX)

set (CMAKE_CXX_STANDARD 14)
set (THREADS_PREFER_PTHREAD_FLAG ON)
find_package(Threads REQUIRED)
add_library(IAdapter_${compName} IAdapter_${compName}.cpp)
target_link_libraries(IAdapter_${compName} ${compName})
target_include_directories(IAdapter_${compName} PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})

add_executable(Coordinator_${compName} Coordinator_${compName}.cpp)
set_target_properties(Coordinator_${compName} PROPERTIES LINKER_LANGUAGE CXX)
target_link_libraries(Coordinator_${compName} ${targets} Threads::Threads IAdapter_${compName})
target_include_directories(Coordinator_${compName} PUBLIC ${CMAKE_CURRENT_SOURCE_DIR})

export(TARGETS Coordinator_${compName} IAdapter_${compName} FILE Coordinator_${compName}.cmake)

install(TARGETS Coordinator_${compName} DESTINATION bin)
