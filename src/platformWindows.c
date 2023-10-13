#include "duru.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <windows.h>

DuruByte* duruGetCwdName() {
    DuruByte* cwd     = duruAllocateArena(MAX_PATH, 1);
    DuruSize  cwdSize = GetCurrentDirectoryA(MAX_PATH, cwd);
    if (!cwdSize) {
        (void)fputs(
          "failure: Could not get the current working directory!\n", stderr);
        abort();
    }
    cwdSize++;
    DuruSize nameIndex;
    for (nameIndex = cwdSize; nameIndex != 0; nameIndex--) {
        if (cwd[nameIndex - 1] == '\\') { break; }
    }
    return cwd + nameIndex;
}
