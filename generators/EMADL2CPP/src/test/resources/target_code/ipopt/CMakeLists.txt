cmake_minimum_required(VERSION 3.5)
set(CMAKE_CXX_STANDARD 14)

project(ipopt_quadraticOpt LANGUAGES CXX)

#set cmake module path
set(CMAKE_MODULE_PATH ${CMAKE_MODULE_PATH} ${CMAKE_CURRENT_SOURCE_DIR}/cmake)

# add dependencies
find_package(Armadillo REQUIRED)
set(INCLUDE_DIRS ${INCLUDE_DIRS} ${Armadillo_INCLUDE_DIRS})
set(LIBS ${LIBS} ${Armadillo_LIBRARIES})

find_package(CPPAD REQUIRED)
set(INCLUDE_DIRS ${INCLUDE_DIRS} ${CPPAD_INCLUDE_DIRS})


find_package(Ipopt REQUIRED)
set(INCLUDE_DIRS ${INCLUDE_DIRS} ${Ipopt_INCLUDE_DIRS})
set(LIBS ${LIBS} ${Ipopt_LIBRARIES})

find_package(CoinMumps REQUIRED)

set(LIBS ${LIBS} ${CoinMumps_LIBRARIES})

find_package(CoinLapack REQUIRED)

set(LIBS ${LIBS} ${CoinLapack_LIBRARIES})

find_package(CoinBlas REQUIRED)

set(LIBS ${LIBS} ${CoinBlas_LIBRARIES})

find_package(CoinMetis REQUIRED)

set(LIBS ${LIBS} ${CoinMetis_LIBRARIES})

find_package(GFortran REQUIRED)

set(LIBS ${LIBS} ${GFortran_LIBRARIES})
set(LIBS ${LIBS} "quadmath")

# additional library linkage

# additional commands

# create static library
include_directories(${INCLUDE_DIRS})
add_library(ipopt_quadraticOpt ipopt_quadraticOpt.cpp)
target_include_directories(ipopt_quadraticOpt PUBLIC ${CMAKE_CURRENT_SOURCE_DIR} ${INCLUDE_DIRS})
target_link_libraries(ipopt_quadraticOpt PUBLIC ${LIBS})
set_target_properties(ipopt_quadraticOpt PROPERTIES LINKER_LANGUAGE CXX)

# export cmake project
export(TARGETS ipopt_quadraticOpt FILE ipopt_quadraticOpt.cmake)

# additional commands end
