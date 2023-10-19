#include "internal.h"

#include <stdio.h>
#include <stdlib.h>

#define duruMainFile duruView("src/main.duru")

void duruInitialize() {
    DuruString projectFile = {};
    duruAppend(&projectFile, duruView("project "));
    DuruString cwd = {};
    duruAppendCwd(&cwd);
    DuruStringView cwdName = duruGetFileName(duruViewString(cwd));
    duruAppend(&projectFile, cwdName);
    duruAppend(&projectFile, duruView(" {\n"));
    duruAppend(&projectFile, duruView("    executable "));
    duruAppend(&projectFile, cwdName);
    duruAppend(&projectFile, duruView(";\n"));
    duruAppend(&projectFile, duruView("}\n"));
    duruStoreFile(duruProjectFile, duruViewString(projectFile));
    duruDestroyString(projectFile);
    duruEnsureDirectory(duruSourceDirectory);
    duruStoreFile(
      duruMainFile,
      duruView("main() {\n"
               "    duru.print(\"Hello, World!\\n\");\n"
               "}\n"));
}
