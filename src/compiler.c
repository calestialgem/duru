#include "internal.h"

#include <stdio.h>
#include <string.h>

void duruCompile() {
    DuruString projectFile = {};
    duruLoadFile(duruProjectFile, &projectFile);
    DuruProjectConfiguration projectConfiguration = {};
    duruParseProjectConfiguration(
      &projectConfiguration, duruViewString(projectFile));
    printf(
      "Project: `%.*s`\n",
      (int)projectConfiguration.name.size,
      projectConfiguration.name.bytes);
    duruDestroyProjectConfiguration(projectConfiguration);
    duruDestroyString(projectFile);
    duruRecreateDirectory(duruArtifactDirectory);
}
