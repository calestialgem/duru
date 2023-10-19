cmake_minimum_required(VERSION 3.21)
project(duru C)

add_executable(${PROJECT_NAME}
  src/compiler.c
  src/debug.c
  src/initializer.c
  src/main.c
  src/platform.c
  src/projectConfiguration.c
  src/string.c
  src/stringList.c
  src/stringView.c
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
  -Wno-gnu-zero-variadic-macro-arguments
  -Wno-microsoft-enum-forward-reference
  -Wno-gnu-empty-initializer
)
