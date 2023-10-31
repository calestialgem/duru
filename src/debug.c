// Utilities for debugging the tool.

#include "duru.h"

#include <errno.h>
#include <stdarg.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <vadefs.h>

void duruCrashWithLocation(
  char const* function, char const* file, int line, char const* format, ...) {
    errno_t errorNumber = errno;
    size_t  suffix      = strlen(__FILE__) - strlen("debug.c");
    (void)fprintf(
      stderr, "in %s at %s:%i\nfailure: ", function, file + suffix, line);
    va_list arguments = {0};
    va_start(arguments, format);
    if (vfprintf(stderr, format, arguments) < 0) {
        (void)fprintf(stderr, "Could not format the message `%s`!", format);
    }
    va_end(arguments);
    (void)fputc('\n', stderr);
    if (errorNumber) {
        errno = errorNumber;
        perror("cause");
    }
    abort();
}
