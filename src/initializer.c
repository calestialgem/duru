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
    duruDestroyString(cwd);
    duruAppend(&projectFile, duruView(" {}\n"));
    duruStoreFile(duruProjectFile, duruViewString(projectFile));
    duruDestroyString(projectFile);
    duruEnsureDirectory(duruView("src"));
    duruStoreFile(
      duruMainFile,
      duruView("using duru.Entrypoint;\n"
               "using duru.print;\n"
               "\n"
               "@Entrypoint\n"
               "main() {\n"
               "    print(\"Hello, World!\\n\");\n"
               "}\n"));
}
