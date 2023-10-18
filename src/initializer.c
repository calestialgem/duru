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
            fprintf(projectFile, "project %s;\n", cwdName) >= 0,
            "Could not write to the file `%s`!",
            duruProjectFileName);
    duruEnsure(
            !fclose(projectFile),
            "Could not write to the file `%s`!",
            duruProjectFileName);
    duruEnsureDirectory("src");
    char* packageName = duruJoin("src", "/", cwdName);
    duruEnsureDirectory(packageName);
    char* mainFilePath = duruJoin(packageName, "/", duruMainFileName);
    FILE* mainFile     = fopen(mainFilePath, "wx");
    duruEnsure(mainFile, "Could not open the file `%s`!", mainFilePath);
    duruEnsure(
            fprintf(mainFile, "entrypoint {}\n") >= 0,
            "Could not write to the file `%s`!",
            mainFilePath);
    duruEnsure(
            !fclose(mainFile),
            "Could not write to the file `%s`!",
            mainFilePath);
    free(mainFilePath);
    free(packageName);
    free(cwdName);
}
