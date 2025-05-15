# Automatically generated file
#
# - Try to find Armadillo
# Once done this will define
#  Armadillo_FOUND - System has Armadillo
#  Armadillo_INCLUDE_DIRS - The Armadillo include directories
#  Armadillo_LIBRARY_DIRS - The library directories needed to use Armadillo
#  Armadillo_LIBRARIES    - The libraries needed to use Armadillo

find_path(Armadillo_INCLUDE_DIR
        NAMES armadillo
        PATH_SUFFIXES "include" 
        PATHS
        HINTS $ENV{Armadillo_HOME} 
        )
find_library(Armadillo_LIBRARY
        NAMES armadillo
        PATH_SUFFIXES "lib" "lib64" "lib/x86_64-linux-gnu" "examples/lib_win64" "build" "Release" "x64" "x86" 
        PATHS
        HINTS $ENV{Armadillo_HOME} 
        )

include(FindPackageHandleStandardArgs)
# if all listed variables are TRUE
find_package_handle_standard_args(
  Armadillo
  DEFAULT_MSG
  Armadillo_INCLUDE_DIR
  Armadillo_LIBRARY
  )

mark_as_advanced(
  Armadillo_INCLUDE_DIR
  Armadillo_LIBRARY
  )

set(Armadillo_INCLUDE_DIRS ${Armadillo_INCLUDE_DIR})
set(Armadillo_LIBRARIES ${Armadillo_LIBRARY})
