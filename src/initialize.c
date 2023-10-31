// Implements project initialization command.

#include "duru.h"

#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <vadefs.h>

// Create the project's configuration file.
static void createConfiguration(char const* projectName);

// Create the project's source directory and write the main source file.
static void createMainSource();

// Creates a new file at the given path and writes to formatted string to the
// file. Fails if the file already exists.
[[gnu::format(printf, 2, 3)]] static void writeString(
  char const* path, char const* format, ...);

void duruInitialize() {
    char* cwd = duruGetCurrentDirectory();
    createConfiguration(duruGetFileName(cwd));
    createMainSource();
    free(cwd);
}

static void createConfiguration(char const* projectName) {
    writeString(
      "project.duru",
      "project %s {\n"
      "  executable %s;\n"
      "}\n",
      projectName,
      projectName);
}

static void createMainSource() {
    // Path to the project's source directory.
    char const sourceDirectory[] = "src";

    duruEnsureDirectory(sourceDirectory);
    duruEnterDirectory(sourceDirectory);

    writeString(
      "main.duru",
      "entrypoint {\n"
      "  duru.print(\"Hello, World!\\n\");\n"
      "}\n");
}

static void writeString(char const* path, char const* format, ...) {
    FILE* file = {0};
    if (fopen_s(&file, path, "wx")) {
        duruCrash("Could not write to the file at `%s`!", path);
    }
    va_list arguments = {0};
    va_start(arguments, format);
    if (vfprintf(file, format, arguments) < 0) {
        duruCrash("Could not format `%s` to the file at `%s`!", format, path);
    }
    va_end(arguments);
    if (fclose(file)) {
        duruCrash("Could not write to the file at `%s`!", path);
    }
}
