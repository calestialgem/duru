// Declarations of the tool for the Duru Programming Language.

#pragma once

// Creates an allocated, null-terminated string via formatting the givens.
[[gnu::format(printf, 1, 2)]] char* duruFormat(char const* format, ...);

// Returns the name of the file at the given path.
char const* duruGetFileName(char const* path);

// Returns the current working directory.
char* duruGetCurrentDirectory();

// Changes the current working directory to the given path.
void duruEnterDirectory(char const* path);

// Creates a directory at the given path if it does not exist.
void duruEnsureDirectory(char const* path);

// Creates a directory at the given path before deleting it if it exists.
void duruRecreateDirectory(char const* path);

// Sets up a new Duru project in the current working directory.
void duruInitialize();

// Compiles the Duru project in the current working directory.
void duruBuild();

// Prints the error message and aborts the program.
//
// Use `duruCrash` macro instead of manually passing the location.
[[noreturn, gnu::format(printf, 4, 5)]] void duruCrashWithLocation(
  char const* function, char const* file, int line, char const* format, ...);

// Calls `duruCrashWithLocation` with the invoker's location.
#define duruCrash(format, ...)                                                 \
    duruCrashWithLocation(__func__, __FILE__, __LINE__, format, __VA_ARGS__)
