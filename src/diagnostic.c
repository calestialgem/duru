#include "duru.h"

#include <errno.h>
#include <stdarg.h>
#include <stdio.h>
#include <stdlib.h>
#include <vadefs.h>

#define duruTitle           "failure: "
#define duruFallbackMessage "Could not format the failure message!"

DuruDiagnostic duruCreateDiagnostic(
  char const* function, char const* file, int line, char const* format, ...) {
    errno_t errorNo = errno;
    errno           = 0;
    va_list arguments;
    va_start(arguments, format);
    ptrdiff_t messageSize = vsnprintf(0, 0, format, arguments);
    va_end(arguments);
    ptrdiff_t actualMessageSize = messageSize;
    if (messageSize < 0) { actualMessageSize = strlen(duruFallbackMessage); }
    ptrdiff_t causeSize = 0;
    if (errorNo) {
        causeSize = snprintf(0, 0, "cause: %s\n", strerror(errorNo));
    }
    ptrdiff_t traceSize =
      snprintf(0, 0, "  at %s in %s:%d\n", function, file, line);
    ptrdiff_t size =
      strlen(duruTitle) + actualMessageSize + 1 + causeSize + traceSize;
    char* bytes = malloc(size + 1);
    if (!bytes) {
        (void)fprintf(
          stderr,
          "failure: Could not allocate a string of size %zi for creating a diagnostic that originated at %s in %s:%i!\n",
          size,
          function,
          file,
          line);
        abort();
    }
    ptrdiff_t index  = 0;
    index           += sprintf(&bytes[index], duruTitle);
    if (messageSize < 0) {
        index += sprintf(&bytes[index], duruFallbackMessage);
    } else {
        va_start(arguments, format);
        index += vsprintf(&bytes[index], format, arguments);
        va_end(arguments);
    }
    bytes[index++] = '\n';
    if (errorNo) {
        index += sprintf(&bytes[index], "cause: %s\n", strerror(errorNo));
    }
    index += sprintf(&bytes[index], "  at %s in %s:%i\n", function, file, line);
    return (DuruDiagnostic){.bytes = bytes, .capacity = size, .size = size};
}
