cmake_minimum_required(VERSION 3.21)
project(duru C)

## The main executable, which is the duru tool.
add_executable(${PROJECT_NAME}
  src/main.c
)
set_target_properties(${PROJECT_NAME} PROPERTIES
  C_STANDARD 23
  C_STANDARD_REQUIRED on
  C_EXTENSIONS off
)
target_compile_options(${PROJECT_NAME} PRIVATE
  -Wall
  -Wextra
  -Wpedantic
)
