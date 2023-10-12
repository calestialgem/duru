#include "duru.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <windows.h>

char* duruGetCwdName() {
    char   cwd[MAX_PATH];
    size_t cwdSize = GetCurrentDirectoryA(MAX_PATH, cwd);
    if (!cwdSize) {
        (void)fputs(
          "failure: Could not get the current working directory!\n", stderr);
        abort();
    }
    cwdSize++;
    size_t nameIndex;
    for (nameIndex = cwdSize; nameIndex != 0; nameIndex--) {
        if (cwd[nameIndex - 1] == '\\') { break; }
    }
    size_t nameSize = cwdSize - nameIndex;
    char*  cwdName  = malloc(nameSize);
    if (!cwdName) {
        (void)fputs("failure: Could not allocate!\n", stderr);
        abort();
    }
    memcpy(cwdName, cwd + nameIndex, nameSize);
    return cwdName;
}
