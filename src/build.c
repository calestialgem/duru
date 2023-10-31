// Implements project build command.

#include "duru.h"

#include <stdio.h>
#include <stdlib.h>

void duruBuild() {
    char* configuration = duruLoadFile(duruConfigurationPath);
    puts(configuration);
    free(configuration);
}
