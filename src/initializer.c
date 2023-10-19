#include "internal.h"

#include <stdio.h>
#include <stdlib.h>

#define duruMainFile "src/main.duru"

void duruInitialize() {
    char* cwd     = duruGetCwd();
    char* cwdName = duruGetFileName(cwd);
    free(cwd);
    FILE* projectFile = fopen(duruProjectFile, "wx");
    duruEnsure(projectFile, "Could not open the file `%s`!", duruProjectFile);
    duruEnsure(
      fprintf(projectFile, "project %s {}\n", cwdName) >= 0,
      "Could not write to the file `%s`!",
      duruProjectFile);
    duruEnsure(
      !fclose(projectFile),
      "Could not write to the file `%s`!",
      duruProjectFile);
    duruEnsureDirectory("src");
    FILE* mainFile = fopen(duruMainFile, "wx");
    duruEnsure(mainFile, "Could not open the file `%s`!", duruMainFile);
    duruEnsure(
      fprintf(
        mainFile,
        "using duru.Entrypoint;\n"
        "using duru.print;\n"
        "\n"
        "@Entrypoint\n"
        "main() {\n"
        "    print(\"Hello, World!\\n\");\n"
        "}\n")
        >= 0,
      "Could not write to the file `%s`!",
      duruMainFile);
    duruEnsure(
      !fclose(mainFile), "Could not write to the file `%s`!", duruMainFile);
    free(cwdName);
}
