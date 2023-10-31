// Provides string utilities.

#include "duru.h"

#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>

char* duruFormat(char const* format, ...) {
    va_list arguments = {0};
    va_start(arguments, format);
    int length = {0};
    {
        va_list argumentsForCounting = {0};
        va_copy(argumentsForCounting, arguments);
        length = vsnprintf(0, 0, format, argumentsForCounting);
        if (length < 0) { duruCrash("Could not format `%s`!", format); }
        va_end(argumentsForCounting);
    }
    int   capacity = length + 1;
    char* result   = malloc(capacity);
    if (result == NULL) {
        duruCrash(
          "Could not allocate %i bytes for formatting `%s`!", capacity, format);
    }
    if (vsnprintf(result, capacity, format, arguments) != length) {
        duruCrash("Could not format `%s`!", format);
    }
    va_end(arguments);
    return result;
}
