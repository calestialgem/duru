cmake_minimum_required(VERSION 3.21)
project(duru C)

add_executable(${PROJECT_NAME}
  src/initializer.c
  src/main.c
  src/platform.c
  src/string.c
)
set_target_properties(${PROJECT_NAME} PROPERTIES
  C_STANDARD 23
  C_STANDARD_REQUIRED on
  C_EXTENSIONS on
)
target_compile_options(${PROJECT_NAME} PRIVATE
  -Wall
  -Wextra
  -Wpedantic
  -Wno-deprecated-declarations
)
