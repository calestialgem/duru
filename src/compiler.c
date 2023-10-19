#include "internal.h"

#include <stdio.h>

void duruCompile() {
    DuruString projectFile = duruLoadFile(duruProjectFile);
    puts(projectFile.bytes);
    duruDestroyString(projectFile);
}
