#include "duru/diagnostic.h"

#include <errno.h>
#include <stdarg.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

void duruFailWithLocation(
  char const* function,
  char const* file,
  unsigned    line,
  char const* format,
  ...) {
    errno_t errorNumber = errno;
    size_t  sourceDirectoryOffset =
      strlen(__FILE__) - strlen("duru/diagnostic.c");
    (void)fprintf(
      stderr,
      "failure in %s at %s:%u\n",
      function,
      file + sourceDirectoryOffset,
      line);
    va_list arguments = {0};
    va_start(arguments, format);
    (void)vfprintf(stderr, format, arguments);
    va_end(arguments);
    (void)fputc('\n', stderr);
    if (errorNumber != 0) {
        errno = errorNumber;
        perror("cause");
    }
    abort();
}
