cmake_minimum_required(VERSION 3.21)
project(duru C)

add_executable(${PROJECT_NAME})
set_target_properties(${PROJECT_NAME} PROPERTIES
  C_STANDARD 23
  C_STANDARD_REQUIRED on
  C_EXTENSIONS on
)
target_compile_options(${PROJECT_NAME} PRIVATE
  -Wall
  -Wextra
  -Wpedantic
  -Wno-gnu-zero-variadic-macro-arguments
)

target_sources(${PROJECT_NAME} PRIVATE
    src/debug.c
    src/initialize.c
    src/main.c
    src/string.c
)

if(WIN32)
    target_sources(${PROJECT_NAME} PRIVATE
        src/platformWindows.c
    )
else()
    message(FATAL_ERROR "Unsupported platform!")
endif()
