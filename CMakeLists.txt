cmake_minimum_required(VERSION 3.21)
project(duru C)

add_executable(${PROJECT_NAME})
set_target_properties(${PROJECT_NAME} PROPERTIES
  C_STANDARD 23
  C_STANDARD_REQUIRED on
  C_EXTENSIONS on
)
target_include_directories(${PROJECT_NAME} PRIVATE inc)
target_compile_options(${PROJECT_NAME} PRIVATE
  -Wall
  -Wextra
  -Wpedantic
  -Wno-gnu-zero-variadic-macro-arguments
)

add_subdirectory(src)
