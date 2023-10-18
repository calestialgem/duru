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
    if (!projectFile) {
        (void)fprintf(
                stderr,
                "failure: Could not open the file `%s`!\n",
                duruProjectFileName);
        abort();
    }
    if (fprintf(projectFile, "project %s;\n", cwdName) < 0) {
        (void)fprintf(
                stderr,
                "failure: Could not write to the file `%s`!\n",
                duruProjectFileName);
        abort();
    }
    if (fclose(projectFile)) {
        (void)fprintf(
                stderr,
                "failure: Could not write to the file `%s`!\n",
                duruProjectFileName);
        abort();
    }
    duruEnsureDirectory("src");
    char* packageName = duruJoin("src", "/", cwdName);
    duruEnsureDirectory(packageName);
    char* mainFilePath = duruJoin(packageName, "/", duruMainFileName);
    FILE* mainFile     = fopen(mainFilePath, "wx");
    if (!mainFile) {
        (void)fprintf(
                stderr,
                "failure: Could not open the file `%s`!\n",
                mainFilePath);
        abort();
    }
    if (fprintf(mainFile, "entrypoint {}\n") < 0) {
        (void)fprintf(
                stderr,
                "failure: Could not write to the file `%s`!\n",
                mainFilePath);
        abort();
    }
    if (fclose(mainFile)) {
        (void)fprintf(
                stderr,
                "failure: Could not write to the file `%s`!\n",
                mainFilePath);
        abort();
    }
    free(mainFilePath);
    free(packageName);
    free(cwdName);
}
