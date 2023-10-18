#include "internal.h"

#include <stdio.h>
#include <stdlib.h>

#define duruPackageFileName "package.duru"
#define duruMainFileName    "main.duru"

void duruInitialize() {
    char* cwd     = duruGetCwd();
    char* cwdName = duruGetFileName(cwd);
    free(cwd);
    FILE* packageFile = fopen(duruPackageFileName, "wx");
    duruEnsure(
      packageFile, "Could not open the file `%s`!", duruPackageFileName);
    duruEnsure(
      fprintf(packageFile, "package %s {}\n", cwdName) >= 0,
      "Could not write to the file `%s`!",
      duruPackageFileName);
    duruEnsure(
      !fclose(packageFile),
      "Could not write to the file `%s`!",
      duruPackageFileName);
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
