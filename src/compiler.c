#include "internal.h"

#include <stdio.h>

void duruCompile() {
    DuruString packageFileContents = duruLoadFile(duruPackageFile);
    puts(packageFileContents.bytes);
    duruDestroyString(packageFileContents);
}
