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

void duruInitialize() {
    char* cwd = duruGetCurrentDirectory();
    createConfiguration(duruGetFileName(cwd));
    createMainSource();
    free(cwd);
}

static void createConfiguration(char const* projectName) {
    duruStoreFile(
      duruConfigurationPath,
      "project %s {\n"
      "    executable %s;\n"
      "}\n",
      projectName,
      projectName);
}

static void createMainSource() {
    duruEnsureDirectory("src");
    duruStoreFile(
      "src/main.duru",
      "entrypoint {\n"
      "    duru.print(\"Hello, World!\\n\");\n"
      "}\n");
}
