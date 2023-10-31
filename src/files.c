// Provides file related utilities.

#include "duru.h"

#include <stdarg.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>

void duruStoreFile(char const* path, char const* format, ...) {
    FILE* file = {0};
    if (fopen_s(&file, path, "wx")) {
        duruCrash("Could not write to the file at `%s`!", path);
    }
    va_list arguments = {0};
    va_start(arguments, format);
    if (vfprintf(file, format, arguments) < 0) {
        duruCrash("Could not format `%s` to the file at `%s`!", format, path);
    }
    va_end(arguments);
    if (fclose(file)) {
        duruCrash("Could not write to the file at `%s`!", path);
    }
}

char* duruLoadFile(char const* path) {
    enum { initialReadTarget = 1024 };

    FILE* file = {0};
    if (fopen_s(&file, path, "rb")) {
        duruCrash("Could not read from the file at `%s`!", path);
    }
    char*  result     = {0};
    size_t length     = {0};
    size_t readAmount = {0};
    size_t readTarget = initialReadTarget;
    do {
        readTarget         *= 2;
        size_t newCapacity  = length + readTarget + 1;
        result              = realloc(result, newCapacity);
        if (result == NULL) {
            duruCrash(
              "Could not grow the allocation to %zu bytes for reading the file at `%s`!",
              newCapacity,
              path);
        }
        readAmount  = fread(result + length, 1, readTarget, file);
        length     += readAmount;
    } while (readAmount == readTarget);
    result[length] = 0;
    if (!feof(file) || ferror(file) || fclose(file)) {
        duruCrash("Could not read from the file at `%s`!", path);
    }
    return result;
}
