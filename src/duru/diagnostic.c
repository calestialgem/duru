#include "duru/diagnostic.h"

#include <errno.h>
#include <stdarg.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <vadefs.h>

/// String that will be used for prefixing failure messages.
#define duruFailureTitle "failure: "

/// Crashes the program with a fatal error that happened while handling the
/// given diagnostic.
[[noreturn, gnu::format(printf, 2, 3)]] static void duruFatalError(
  DuruDiagnostic* diagnostic, char const* format, ...);

/// Ensures that the given diagnostic has at least given amount of free bytes at
/// the end.
///
/// Grows the diagnostic at least by its current size to amortize allocation
/// costs.
///
/// Crashes when it cannot grow the diagnostic.
static void duruReserve(DuruDiagnostic* diagnostic, int amount);

/// Formats the given location to the end of the given diagnostic.
static void duruAppend(DuruDiagnostic* diagnostic, DuruLocation location);

DuruDiagnostic duruCreateDiagnostic(
  DuruLocation location, char const* format, ...) {
    errno_t        errorNumber = errno;
    DuruDiagnostic diagnostic  = {0};

    duruReserve(&diagnostic, strlen(duruFailureTitle));
    

    va_list arguments = {0};
    va_start(arguments, format);
    (void)vfprintf(stderr, format, arguments);
    va_end(arguments);
    (void)fputc('\n', stderr);
    if (errorNumber != 0) {
        errno = errorNumber;
        perror("cause");
    }

    return diagnostic;
}

void duruAbortOnFailureWithLocation(
  DuruLocation location, DuruDiagnostic diagnostic) {
    if (diagnostic.length == 0) { return; }
    duruAppend(&diagnostic, location);
    (void)fputs(diagnostic.bytes, stderr);
    free(diagnostic.bytes);
    abort();
}

static void duruFatalError(
  DuruDiagnostic* diagnostic, char const* format, ...) {
    errno_t errorNumber = errno;
    (void)fputs(diagnostic->bytes, stderr);
    (void)fputs("fatal error: ", stderr);
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

static void duruReserve(DuruDiagnostic* diagnostic, int amount) {
    int space = diagnostic->size - diagnostic->length;
    if (space >= amount) { return; }
    int growth = amount - space;
    if (growth < diagnostic->size) { growth = diagnostic->size; }
    char* bytes = realloc(diagnostic->bytes, diagnostic->size + growth);
    if (bytes == 0) {
        duruFatalError(
          diagnostic,
          "Could not grow the diagnostic message from %i bytes to %i bytes!",
          diagnostic->size,
          diagnostic->size + growth);
    }
    diagnostic->bytes  = bytes;
    diagnostic->size  += growth;
}

static void duruAppend(DuruDiagnostic* diagnostic, DuruLocation location) {
    int size = snprintf(
      0,
      0,
      "  in %s at %s:%i\n",
      location.function,
      location.file,
      location.line);
    if (size < 0) {
        duruFatalError(
          diagnostic,
          "Could not append the source location `%s %s:%i` to the diagnostic message!",
          location.function,
          location.file,
          location.line);
    }
    duruReserve(diagnostic, size + 1);
    int result = sprintf(
      diagnostic->bytes + diagnostic->length,
      "  in %s at %s:%i\n",
      location.function,
      location.file,
      location.line);
    if (result < 0) {
        duruFatalError(
          diagnostic,
          "Could not append the source location `%s %s:%i` to the diagnostic message!",
          location.function,
          location.file,
          location.line);
    }
}
