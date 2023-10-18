#include "internal.h"

#include <errno.h>
#include <stdarg.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void duruCrash(
        char const* function,
        char const* file,
        unsigned    line,
        char const* format,
        ...) {
    errno_t errorNo = errno;
    va_list arguments;
    va_start(arguments, format);
    (void)vfprintf(stderr, format, arguments);
    va_end(arguments);
    (void)fprintf(stderr, "\n");
    if (errorNo) { (void)fprintf(stderr, "cause: %s\n", strerror(errorNo)); }
    size_t fullSize     = strlen(__FILE__);
    size_t relativeSize = strlen("debug.c");
    (void)fprintf(
            stderr,
            "in %s at %s:%u\n",
            function,
            file + (fullSize - relativeSize),
            line);
    abort();
}
