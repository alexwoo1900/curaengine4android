#Copyright (c) 2020 Ultimaker B.V.
#CuraEngine is released under the terms of the AGPLv3 or higher.

cmake_minimum_required(VERSION 3.10.2)

project("myapplication")

list(APPEND CMAKE_MODULE_PATH "${CMAKE_CURRENT_SOURCE_DIR}/cmake")

#For reading image files.
find_package(Stb REQUIRED)
include_directories(${Stb_INCLUDE_DIRS})

# convert build type to upper case letters
if(CMAKE_BUILD_TYPE)
    string(TOUPPER ${CMAKE_BUILD_TYPE} CMAKE_BUILD_TYPE_UPPER)
endif()

if(CMAKE_BUILD_TYPE_UPPER MATCHES "DEBUG")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${CMAKE_CXX_FLAGS_DEBUG_INIT}")
else()
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} ${CMAKE_CXX_FLAGS_RELEASE_INIT}")
endif()

set(CMAKE_CXX_STANDARD 17)

if(APPLE AND CMAKE_CXX_COMPILER_ID MATCHES "Clang")
    set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -stdlib=libc++")
endif()

if(NOT DEFINED LIB_SUFFIX)
    set(LIB_SUFFIX "")
endif()
set(CMAKE_INSTALL_RPATH "${CMAKE_INSTALL_PREFIX}/lib${LIB_SUFFIX}")

set(CURA_ENGINE_VERSION "master" CACHE STRING "Version name of Cura")

# Add a compiler flag to check the output for insane values if we are in debug mode.
if(CMAKE_BUILD_TYPE_UPPER MATCHES "DEBUG" OR CMAKE_BUILD_TYPE_UPPER MATCHES "RELWITHDEBINFO")
    message(STATUS "Building debug release of CuraEngine.")
    if (NOT MSVC)
        set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wextra -O0 -g -fno-omit-frame-pointer")
    endif()
    add_definitions(-DASSERT_INSANE_OUTPUT)
    add_definitions(-DUSE_CPU_TIME)
    add_definitions(-DDEBUG)
endif()

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -Wno-expansion-to-defined -Wno-unused-private-field -Wno-unused-const-field -Wno-unused-parameter -Wno-unused-lambda -Wno-unused-const-variable")


option (ENABLE_MORE_COMPILER_OPTIMIZATION_FLAGS
    "Enable more optimization flags" ON)
if (ENABLE_MORE_COMPILER_OPTIMIZATION_FLAGS AND NOT (CMAKE_BUILD_TYPE_UPPER MATCHES "DEBUG"))
    message (STATUS "Compile with more optimization flags")
    if (MSVC)
        set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS} /fp:fast")
    else()
        set(CMAKE_CXX_FLAGS_RELEASE "${CMAKE_CXX_FLAGS} -Ofast -funroll-loops")
    endif()
endif ()


include_directories("${CMAKE_CURRENT_BINARY_DIR}" libs libs/clipper)
add_library(clipper SHARED libs/clipper/clipper.cpp)

set(engine_SRCS # Except main.cpp.
    src/Application.cpp
    src/bridge.cpp
    src/ConicalOverhang.cpp
    src/ExtruderTrain.cpp
    src/FffGcodeWriter.cpp
    src/FffPolygonGenerator.cpp
    src/FffProcessor.cpp
    src/gcodeExport.cpp
    src/GCodePathConfig.cpp
    src/infill.cpp
    src/InsetOrderOptimizer.cpp
    src/layerPart.cpp
    src/LayerPlan.cpp
    src/LayerPlanBuffer.cpp
    src/MergeInfillLines.cpp
    src/mesh.cpp
    src/MeshGroup.cpp
    src/Mold.cpp
    src/multiVolumes.cpp
    src/PathOrder.cpp
    src/pathOrderOptimizer.cpp
    src/Preheat.cpp
    src/PrimeTower.cpp
    src/raft.cpp
    src/Scene.cpp
    src/skin.cpp
    src/SkirtBrim.cpp
    src/SupportInfillPart.cpp
    src/Slice.cpp
    src/sliceDataStorage.cpp
    src/slicer.cpp
    src/support.cpp
    src/timeEstimate.cpp
    src/TopSurface.cpp
    src/TreeSupport.cpp
    src/WallsComputation.cpp
    src/wallOverlap.cpp
    src/Weaver.cpp
    src/Wireframe2gcode.cpp

    src/communication/ArcusCommunication.cpp
    src/communication/ArcusCommunicationPrivate.cpp
    src/communication/CommandLine.cpp
    src/communication/Listener.cpp

    src/infill/ImageBasedDensityProvider.cpp
    src/infill/NoZigZagConnectorProcessor.cpp
    src/infill/ZigzagConnectorProcessor.cpp
    src/infill/SierpinskiFill.cpp
    src/infill/SierpinskiFillProvider.cpp
    src/infill/SubDivCube.cpp
    src/infill/GyroidInfill.cpp

    src/pathPlanning/Comb.cpp
    src/pathPlanning/GCodePath.cpp
    src/pathPlanning/LinePolygonsCrossings.cpp
    src/pathPlanning/NozzleTempInsert.cpp
    src/pathPlanning/TimeMaterialEstimates.cpp

    src/progress/Progress.cpp
    src/progress/ProgressStageEstimator.cpp

    src/settings/AdaptiveLayerHeights.cpp
    src/settings/FlowTempGraph.cpp
    src/settings/PathConfigStorage.cpp
    src/settings/Settings.cpp
    src/settings/ZSeamConfig.cpp

    src/utils/AABB.cpp
    src/utils/AABB3D.cpp
    src/utils/Date.cpp
    src/utils/FMatrix4x3.cpp
    src/utils/gettime.cpp
    src/utils/getpath.cpp
    src/utils/LinearAlg2D.cpp
    src/utils/ListPolyIt.cpp
    src/utils/logoutput.cpp
    src/utils/MinimumSpanningTree.cpp
    src/utils/Point3.cpp
    src/utils/PolygonConnector.cpp
    src/utils/PolygonsPointIndex.cpp
    src/utils/PolygonProximityLinker.cpp
    src/utils/polygonUtils.cpp
    src/utils/polygon.cpp
    src/utils/ProximityPointLink.cpp
    src/utils/SVG.cpp
    src/utils/socket.cpp
)

add_library(_CuraEngine SHARED ${engine_SRCS} ${engine_PB_SRCS})

if (CuraEngine_Download_Stb)
    add_dependencies(_CuraEngine stb)
endif()

target_link_libraries(_CuraEngine clipper)
target_link_libraries(_CuraEngine stdc++)


set_target_properties(_CuraEngine PROPERTIES COMPILE_DEFINITIONS "VERSION=\"${CURA_ENGINE_VERSION}\"")


add_library(myapplication SHARED native-lib.cpp)
find_library(log-lib log)
target_link_libraries(myapplication _CuraEngine)
target_link_libraries(_CuraEngine ${log-lib})
target_link_libraries(myapplication ${log-lib})