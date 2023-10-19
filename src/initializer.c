#include "internal.h"

#include <stdio.h>
#include <stdlib.h>

#define duruMainFile "src/main.duru"

void duruInitialize() {
    char* cwd     = duruGetCwd();
    char* cwdName = duruGetFileName(cwd);
    free(cwd);
    FILE* packageFile = fopen(duruPackageFile, "wx");
    duruEnsure(packageFile, "Could not open the file `%s`!", duruPackageFile);
    duruEnsure(
      fprintf(packageFile, "package %s {}\n", cwdName) >= 0,
      "Could not write to the file `%s`!",
      duruPackageFile);
    duruEnsure(
      !fclose(packageFile),
      "Could not write to the file `%s`!",
      duruPackageFile);
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
