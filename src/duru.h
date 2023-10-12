#ifndef DURU_H
#define DURU_H 1

#include <errno.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define duruAbort(format, ...)                                                 \
    do {                                                                       \
        errno_t errorNo = errno;                                               \
        (void)fprintf(                                                         \
          stderr,                                                              \
          __FILE__ ":%d: failure: " format "\n",                               \
          __LINE__,                                                            \
          __VA_ARGS__);                                                        \
        enum { errorBufferSize = 1024 };                                       \
        char errorBuffer[errorBufferSize];                                     \
        if (!strerror_s(errorBuffer, errorBufferSize, errorNo)) {              \
            (void)fprintf(stderr, "caused by: %s\n", errorBuffer);             \
        } else {                                                               \
            (void)fprintf(stderr, "caused by: errno %d\n", errorNo);           \
        }                                                                      \
        abort();                                                               \
    } while (false)

#define duruEnsure(condition, format, ...)                                     \
    do {                                                                       \
        if (!(condition)) { duruAbort(format, __VA_ARGS__); }                  \
    } while (false)

char* duruGetCwdName();
void  duruInitialize();

#endif // DURU_H
