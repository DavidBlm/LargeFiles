cmake_minimum_required(VERSION 3.5.1)

set (CMAKE_CXX_STANDARD 14)

find_package(SWIG REQUIRED)
find_package(Armadillo REQUIRED)
FIND_PACKAGE(PythonLibs 3 REQUIRED)

include(${SWIG_USE_FILE})

set(INCLUDE_DIRS ${INCLUDE_DIRS} ${ARMADILLO_INCLUDE_DIRS} "armanpy" "..")
set(LIBS ${LIBS} ${ARMADILLO_LIBRARIES})
set( CMAKE_SWIG_OUTDIR "${PROJECT_BINARY_DIR}" )

include_directories(${INCLUDE_DIRS} ${PYTHON_INCLUDE_DIRS} .)

add_library(torcs_agent_dqn_reward_executorlib SHARED torcs_agent_dqn_reward_executor.cpp torcs_agent_dqn_reward_executor.h)
target_link_libraries(torcs_agent_dqn_reward_executorlib ${LIBS})

set_source_files_properties( torcs_agent_dqn_reward_executor.i PROPERTIES CPLUSPLUS ON)
set_source_files_properties( torcs_agent_dqn_reward_executor.i PROPERTIES SWIG_FLAGS "-ignoremissing;-w509" )

swig_add_module(torcs_agent_dqn_reward_executor python torcs_agent_dqn_reward_executor.i)
swig_link_libraries(torcs_agent_dqn_reward_executor torcs_agent_dqn_reward_executorlib ${LIBS} ${PYTHON_LIBRARIES})
