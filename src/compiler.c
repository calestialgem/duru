#include "internal.h"

#include <stdio.h>
#include <string.h>

static DuruStringView duruGetProjectName(DuruStringView projectFile);

void duruCompile() {
    DuruString projectFile = {};
    duruLoadFile(duruProjectFile, &projectFile);
    DuruStringView projectName =
      duruGetProjectName(duruViewString(projectFile));
    printf("%.*s\n", (int)projectName.size, projectName.bytes);
    duruDestroyString(projectFile);
}

static DuruStringView duruGetProjectName(DuruStringView projectFile) {
    projectFile = duruRemovePrefix(projectFile, duruView("project "));
    projectFile = duruRemoveSuffix(projectFile, duruView(" {}\n"));
    return projectFile;
}
