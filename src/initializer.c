#include "internal.h"

#include <stdio.h>
#include <stdlib.h>

#define duruProjectFileName "project.duru"
#define duruMainFileName    "main.duru"

void duruInitialize() {
    char* cwd     = duruGetCwd();
    char* cwdName = duruGetFileName(cwd);
    free(cwd);
    FILE* projectFile = fopen(duruProjectFileName, "wx");
    duruEnsure(
      projectFile, "Could not open the file `%s`!", duruProjectFileName);
    duruEnsure(
      fprintf(
        projectFile,
        "project %s {\n"
        "    version 0.1.0;\n"
        "    requires duru;\n"
        "}\n",
        cwdName)
        >= 0,
      "Could not write to the file `%s`!",
      duruProjectFileName);
    duruEnsure(
      !fclose(projectFile),
      "Could not write to the file `%s`!",
      duruProjectFileName);
    duruEnsureDirectory("src");
    char* mainFilePath = duruJoin("src", "/", duruMainFileName);
    FILE* mainFile     = fopen(mainFilePath, "wx");
    duruEnsure(mainFile, "Could not open the file `%s`!", mainFilePath);
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
      mainFilePath);
    duruEnsure(
      !fclose(mainFile), "Could not write to the file `%s`!", mainFilePath);
    free(mainFilePath);
    free(cwdName);
}
