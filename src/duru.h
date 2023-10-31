// Declarations of the tool for the Duru Programming Language.

#pragma once

// Path to the configuration file in the project directory.
#define duruConfigurationPath "project.duru"

// Creates an allocated, null-terminated string via formatting the givens.
[[gnu::format(printf, 1, 2)]] char* duruFormat(char const* format, ...);

// Creates a new file at the given path and writes to formatted string to the
// file. Fails if the file already exists.
[[gnu::format(printf, 2, 3)]] void duruStoreFile(
  char const* path, char const* format, ...);

// Reads and stores the contents of the given file as a byte array.
char* duruLoadFile(char const* path);

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
