# Generated by CMake

if("${CMAKE_MAJOR_VERSION}.${CMAKE_MINOR_VERSION}" LESS 2.6)
   message(FATAL_ERROR "CMake >= 2.6.0 required")
endif()
cmake_policy(PUSH)
cmake_policy(VERSION 2.6...3.20)
#----------------------------------------------------------------
# Generated CMake target import file.
#----------------------------------------------------------------

# Commands may need to know the format version.
set(CMAKE_IMPORT_FILE_VERSION 1)

# Protect against multiple inclusion, which would fail when already imported targets are added once more.
set(_targetsDefined)
set(_targetsNotDefined)
set(_expectedTargets)
foreach(_expectedTarget libcpufeatures libjpeg-turbo libtiff libwebp libopenjp2 libpng IlmImf tbb ippiw libprotobuf ittnotify opencv_core opencv_flann opencv_imgproc opencv_photo ocv.3rdparty.flatbuffers opencv_dnn opencv_features opencv_imgcodecs ocv.3rdparty.android_mediandk opencv_videoio opencv_3d opencv_highgui opencv_objdetect opencv_stereo opencv_stitching opencv_video opencv_calib opencv_java)
  list(APPEND _expectedTargets ${_expectedTarget})
  if(NOT TARGET ${_expectedTarget})
    list(APPEND _targetsNotDefined ${_expectedTarget})
  endif()
  if(TARGET ${_expectedTarget})
    list(APPEND _targetsDefined ${_expectedTarget})
  endif()
endforeach()
if("${_targetsDefined}" STREQUAL "${_expectedTargets}")
  unset(_targetsDefined)
  unset(_targetsNotDefined)
  unset(_expectedTargets)
  set(CMAKE_IMPORT_FILE_VERSION)
  cmake_policy(POP)
  return()
endif()
if(NOT "${_targetsDefined}" STREQUAL "")
  message(FATAL_ERROR "Some (but not all) targets in this export set were already defined.\nTargets Defined: ${_targetsDefined}\nTargets not yet defined: ${_targetsNotDefined}\n")
endif()
unset(_targetsDefined)
unset(_targetsNotDefined)
unset(_expectedTargets)


# Compute the installation prefix relative to this file.
get_filename_component(_IMPORT_PREFIX "${CMAKE_CURRENT_LIST_FILE}" PATH)
get_filename_component(_IMPORT_PREFIX "${_IMPORT_PREFIX}" PATH)
get_filename_component(_IMPORT_PREFIX "${_IMPORT_PREFIX}" PATH)
get_filename_component(_IMPORT_PREFIX "${_IMPORT_PREFIX}" PATH)
get_filename_component(_IMPORT_PREFIX "${_IMPORT_PREFIX}" PATH)
if(_IMPORT_PREFIX STREQUAL "/")
  set(_IMPORT_PREFIX "")
endif()

# Create imported target libcpufeatures
add_library(libcpufeatures STATIC IMPORTED)

# Create imported target libjpeg-turbo
add_library(libjpeg-turbo STATIC IMPORTED)

# Create imported target libtiff
add_library(libtiff STATIC IMPORTED)

set_target_properties(libtiff PROPERTIES
  INTERFACE_LINK_LIBRARIES "z"
)

# Create imported target libwebp
add_library(libwebp STATIC IMPORTED)

set_target_properties(libwebp PROPERTIES
  INTERFACE_LINK_LIBRARIES "libcpufeatures"
)

# Create imported target libopenjp2
add_library(libopenjp2 STATIC IMPORTED)

set_target_properties(libopenjp2 PROPERTIES
  INTERFACE_COMPILE_DEFINITIONS "OPJ_STATIC"
  INTERFACE_LINK_LIBRARIES "\$<LINK_ONLY:m>;\$<LINK_ONLY:-pthread>"
)

# Create imported target libpng
add_library(libpng STATIC IMPORTED)

set_target_properties(libpng PROPERTIES
  INTERFACE_LINK_LIBRARIES "z"
)

# Create imported target IlmImf
add_library(IlmImf STATIC IMPORTED)

set_target_properties(IlmImf PROPERTIES
  INTERFACE_LINK_LIBRARIES "z"
)

# Create imported target tbb
add_library(tbb STATIC IMPORTED)

set_target_properties(tbb PROPERTIES
  INTERFACE_COMPILE_DEFINITIONS "TBB_USE_GCC_BUILTINS=1;__TBB_GCC_BUILTIN_ATOMICS_PRESENT=1;TBB_SUPPRESS_DEPRECATED_MESSAGES=1"
  INTERFACE_LINK_LIBRARIES "c;m;dl"
)

# Create imported target ippiw
add_library(ippiw STATIC IMPORTED)

# Create imported target libprotobuf
add_library(libprotobuf STATIC IMPORTED)

set_target_properties(libprotobuf PROPERTIES
  INTERFACE_LINK_LIBRARIES "-landroid;-llog"
)

# Create imported target ittnotify
add_library(ittnotify STATIC IMPORTED)

set_target_properties(ittnotify PROPERTIES
  INTERFACE_LINK_LIBRARIES "dl"
)

# Create imported target opencv_core
add_library(opencv_core STATIC IMPORTED)

set_target_properties(opencv_core PROPERTIES
  INTERFACE_LINK_LIBRARIES "\$<LINK_ONLY:dl>;\$<LINK_ONLY:m>;\$<LINK_ONLY:log>;\$<LINK_ONLY:ippiw>;\$<LINK_ONLY:ippicv>;\$<LINK_ONLY:tbb>;\$<LINK_ONLY:z>;\$<LINK_ONLY:libcpufeatures>;\$<LINK_ONLY:ittnotify>"
)

# Create imported target opencv_flann
add_library(opencv_flann STATIC IMPORTED)

set_target_properties(opencv_flann PROPERTIES
  INTERFACE_LINK_LIBRARIES "opencv_core;opencv_core;\$<LINK_ONLY:dl>;\$<LINK_ONLY:m>;\$<LINK_ONLY:log>;\$<LINK_ONLY:ippiw>;\$<LINK_ONLY:ippicv>"
)

# Create imported target opencv_imgproc
add_library(opencv_imgproc STATIC IMPORTED)

set_target_properties(opencv_imgproc PROPERTIES
  INTERFACE_LINK_LIBRARIES "opencv_core;opencv_core;\$<LINK_ONLY:dl>;\$<LINK_ONLY:m>;\$<LINK_ONLY:log>;\$<LINK_ONLY:ippiw>;\$<LINK_ONLY:ippicv>;\$<LINK_ONLY:z>"
)

# Create imported target opencv_photo
add_library(opencv_photo STATIC IMPORTED)

set_target_properties(opencv_photo PROPERTIES
  INTERFACE_LINK_LIBRARIES "opencv_core;opencv_imgproc;opencv_core;opencv_imgproc;\$<LINK_ONLY:dl>;\$<LINK_ONLY:m>;\$<LINK_ONLY:log>;\$<LINK_ONLY:ippiw>;\$<LINK_ONLY:ippicv>"
)

# Create imported target ocv.3rdparty.flatbuffers
add_library(ocv.3rdparty.flatbuffers INTERFACE IMPORTED)

set_target_properties(ocv.3rdparty.flatbuffers PROPERTIES
  INTERFACE_COMPILE_DEFINITIONS "HAVE_FLATBUFFERS=1"
)

# Create imported target opencv_dnn
add_library(opencv_dnn STATIC IMPORTED)

set_target_properties(opencv_dnn PROPERTIES
  INTERFACE_LINK_LIBRARIES "opencv_core;opencv_imgproc;opencv_core;opencv_imgproc;\$<LINK_ONLY:dl>;\$<LINK_ONLY:m>;\$<LINK_ONLY:log>;\$<LINK_ONLY:ippiw>;\$<LINK_ONLY:ippicv>;\$<LINK_ONLY:ocv.3rdparty.flatbuffers>;\$<LINK_ONLY:libprotobuf>"
)

# Create imported target opencv_features
add_library(opencv_features STATIC IMPORTED)

set_target_properties(opencv_features PROPERTIES
  INTERFACE_LINK_LIBRARIES "opencv_core;opencv_flann;opencv_imgproc;opencv_core;opencv_flann;opencv_imgproc;\$<LINK_ONLY:dl>;\$<LINK_ONLY:m>;\$<LINK_ONLY:log>;\$<LINK_ONLY:ippiw>;\$<LINK_ONLY:ippicv>"
)

# Create imported target opencv_imgcodecs
add_library(opencv_imgcodecs STATIC IMPORTED)

set_target_properties(opencv_imgcodecs PROPERTIES
  INTERFACE_LINK_LIBRARIES "opencv_core;opencv_imgproc;opencv_core;opencv_imgproc;\$<LINK_ONLY:dl>;\$<LINK_ONLY:m>;\$<LINK_ONLY:log>;\$<LINK_ONLY:ippiw>;\$<LINK_ONLY:ippicv>;\$<LINK_ONLY:libjpeg-turbo>;\$<LINK_ONLY:libwebp>;\$<LINK_ONLY:libpng>;\$<LINK_ONLY:libtiff>;\$<LINK_ONLY:libopenjp2>;\$<LINK_ONLY:IlmImf>;\$<LINK_ONLY:z>"
)

# Create imported target ocv.3rdparty.android_mediandk
add_library(ocv.3rdparty.android_mediandk INTERFACE IMPORTED)

set_target_properties(ocv.3rdparty.android_mediandk PROPERTIES
  INTERFACE_COMPILE_DEFINITIONS "HAVE_ANDROID_MEDIANDK"
  INTERFACE_LINK_LIBRARIES "-landroid -llog -lmediandk"
)

# Create imported target opencv_videoio
add_library(opencv_videoio STATIC IMPORTED)

set_target_properties(opencv_videoio PROPERTIES
  INTERFACE_LINK_LIBRARIES "opencv_core;opencv_imgproc;opencv_imgcodecs;opencv_core;opencv_imgproc;opencv_imgcodecs;\$<LINK_ONLY:dl>;\$<LINK_ONLY:m>;\$<LINK_ONLY:log>;\$<LINK_ONLY:ippiw>;\$<LINK_ONLY:ippicv>;\$<LINK_ONLY:ocv.3rdparty.android_mediandk>"
)

# Create imported target opencv_3d
add_library(opencv_3d STATIC IMPORTED)

set_target_properties(opencv_3d PROPERTIES
  INTERFACE_LINK_LIBRARIES "opencv_core;opencv_flann;opencv_imgproc;opencv_features;opencv_core;opencv_flann;opencv_imgproc;opencv_features;\$<LINK_ONLY:dl>;\$<LINK_ONLY:m>;\$<LINK_ONLY:log>;\$<LINK_ONLY:ippiw>;\$<LINK_ONLY:ippicv>"
)

# Create imported target opencv_highgui
add_library(opencv_highgui STATIC IMPORTED)

set_target_properties(opencv_highgui PROPERTIES
  INTERFACE_LINK_LIBRARIES "opencv_core;opencv_imgproc;opencv_imgcodecs;opencv_videoio;opencv_core;opencv_imgproc;opencv_imgcodecs;opencv_videoio;\$<LINK_ONLY:dl>;\$<LINK_ONLY:m>;\$<LINK_ONLY:log>;\$<LINK_ONLY:ippiw>;\$<LINK_ONLY:ippicv>"
)

# Create imported target opencv_objdetect
add_library(opencv_objdetect STATIC IMPORTED)

set_target_properties(opencv_objdetect PROPERTIES
  INTERFACE_LINK_LIBRARIES "opencv_core;opencv_flann;opencv_imgproc;opencv_dnn;opencv_features;opencv_3d;opencv_core;opencv_flann;opencv_imgproc;opencv_dnn;opencv_features;opencv_3d;\$<LINK_ONLY:dl>;\$<LINK_ONLY:m>;\$<LINK_ONLY:log>;\$<LINK_ONLY:ippiw>;\$<LINK_ONLY:ippicv>"
)

# Create imported target opencv_stereo
add_library(opencv_stereo STATIC IMPORTED)

set_target_properties(opencv_stereo PROPERTIES
  INTERFACE_LINK_LIBRARIES "opencv_core;opencv_flann;opencv_imgproc;opencv_features;opencv_3d;opencv_core;opencv_flann;opencv_imgproc;opencv_features;opencv_3d;\$<LINK_ONLY:dl>;\$<LINK_ONLY:m>;\$<LINK_ONLY:log>;\$<LINK_ONLY:ippiw>;\$<LINK_ONLY:ippicv>"
)

# Create imported target opencv_stitching
add_library(opencv_stitching STATIC IMPORTED)

set_target_properties(opencv_stitching PROPERTIES
  INTERFACE_LINK_LIBRARIES "opencv_core;opencv_flann;opencv_imgproc;opencv_features;opencv_3d;opencv_core;opencv_flann;opencv_imgproc;opencv_features;opencv_3d;\$<LINK_ONLY:dl>;\$<LINK_ONLY:m>;\$<LINK_ONLY:log>;\$<LINK_ONLY:ippiw>;\$<LINK_ONLY:ippicv>"
)

# Create imported target opencv_video
add_library(opencv_video STATIC IMPORTED)

set_target_properties(opencv_video PROPERTIES
  INTERFACE_LINK_LIBRARIES "opencv_core;opencv_flann;opencv_imgproc;opencv_dnn;opencv_features;opencv_3d;opencv_core;opencv_flann;opencv_imgproc;opencv_dnn;opencv_features;opencv_3d;\$<LINK_ONLY:dl>;\$<LINK_ONLY:m>;\$<LINK_ONLY:log>;\$<LINK_ONLY:ippiw>;\$<LINK_ONLY:ippicv>"
)

# Create imported target opencv_calib
add_library(opencv_calib STATIC IMPORTED)

set_target_properties(opencv_calib PROPERTIES
  INTERFACE_LINK_LIBRARIES "opencv_core;opencv_flann;opencv_imgproc;opencv_features;opencv_3d;opencv_stereo;opencv_core;opencv_flann;opencv_imgproc;opencv_features;opencv_3d;opencv_stereo;\$<LINK_ONLY:dl>;\$<LINK_ONLY:m>;\$<LINK_ONLY:log>;\$<LINK_ONLY:ippiw>;\$<LINK_ONLY:ippicv>"
)

# Create imported target opencv_java
add_library(opencv_java SHARED IMPORTED)

set_target_properties(opencv_java PROPERTIES
  INTERFACE_LINK_LIBRARIES "jnigraphics;jnigraphics;log;dl;z;log;dl;z"
)

if(CMAKE_VERSION VERSION_LESS 3.0.0)
  message(FATAL_ERROR "This file relies on consumers using CMake 3.0.0 or greater.")
endif()

# Load information for each installed configuration.
get_filename_component(_DIR "${CMAKE_CURRENT_LIST_FILE}" PATH)
file(GLOB CONFIG_FILES "${_DIR}/OpenCVModules-*.cmake")
foreach(f ${CONFIG_FILES})
  include(${f})
endforeach()

# Cleanup temporary variables.
set(_IMPORT_PREFIX)

# Loop over all imported files and verify that they actually exist
foreach(target ${_IMPORT_CHECK_TARGETS} )
  foreach(file ${_IMPORT_CHECK_FILES_FOR_${target}} )
    if(NOT EXISTS "${file}" )
      message(FATAL_ERROR "The imported target \"${target}\" references the file
   \"${file}\"
but this file does not exist.  Possible reasons include:
* The file was deleted, renamed, or moved to another location.
* An install or uninstall procedure did not complete successfully.
* The installation package was faulty and contained
   \"${CMAKE_CURRENT_LIST_FILE}\"
but not all the files it references.
")
    endif()
  endforeach()
  unset(_IMPORT_CHECK_FILES_FOR_${target})
endforeach()
unset(_IMPORT_CHECK_TARGETS)

# This file does not depend on other imported targets which have
# been exported from the same project but in a separate export set.

# Commands beyond this point should not need to know the version.
set(CMAKE_IMPORT_FILE_VERSION)
cmake_policy(POP)
