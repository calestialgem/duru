// Implements project initialization command.

#include "duru.h"

#include <stdio.h>
#include <stdlib.h>

void duruInitialize() {
    char*       cwd         = duruGetCurrentDirectory();
    char const* projectName = duruGetFileName(cwd);
    puts(projectName);
    free(cwd);
}
