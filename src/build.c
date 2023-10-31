// Implements project build command.

#include "duru.h"

#include <stdio.h>
#include <stdlib.h>

void duruBuild() {
    char* configurationContents = duruLoadFile(duruConfigurationPath);
    DuruConfiguration* configuration =
      duruParseConfiguration(configurationContents);
    free(configuration);
    free(configurationContents);
}
