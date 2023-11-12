cmake_minimum_required(VERSION 3.12)
project(duru CXX)
add_executable(${PROJECT_NAME}
  src/main.cxx
  src/initializer/initialize.cxx
)
target_include_directories(${PROJECT_NAME} PUBLIC inc)
set_target_properties(${PROJECT_NAME} PROPERTIES
  CXX_STANDARD 20
  CXX_STANDARD_REQUIRED on
  CXX_EXTENSIONS off
)
target_compile_options(${PROJECT_NAME} PRIVATE
  -Wall
  -Wextra
  -Wpedantic
)
