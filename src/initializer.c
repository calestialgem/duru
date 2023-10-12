#include "duru.h"

#include <stdio.h>
#include <stdlib.h>

void duruInitialize() {
    char* cwdName = duruGetCwdName();
    FILE* configurationFile;
    duruEnsure(
      fopen_s(&configurationFile, "project.duru", "wx") == 0,
      "Could not create the configuration file!");
    duruEnsure(
      fprintf(configurationFile, "name: %s\n", cwdName) >= 0,
      "Could not write to the configuration file!");
    duruEnsure(
      fclose(configurationFile) == 0,
      "Could not write to the configuration file!");
}
