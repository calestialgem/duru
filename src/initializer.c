#include "internal.h"

#include <stdio.h>
#include <stdlib.h>

#define duruMainFile duruView("src/main.duru")

void duruInitialize() {
    DuruString     cwd         = duruGetCwd();
    DuruStringView cwdName     = duruGetFileName(duruViewString(cwd));
    DuruString     projectFile = {};
    duruAppend(&projectFile, duruView("project "));
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
