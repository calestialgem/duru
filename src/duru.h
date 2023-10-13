#ifndef DURU_H
#define DURU_H 1

#include <errno.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef char   DuruByte;
typedef size_t DuruSize;

#define duruAbort(format, ...)                                                 \
    do {                                                                       \
        errno_t errorNo = errno;                                               \
        (void)fprintf(                                                         \
          stderr,                                                              \
          __FILE__ ":%d: failure: " format "\n",                               \
          __LINE__,                                                            \
          __VA_ARGS__);                                                        \
        enum { errorBufferSize = 1024 };                                       \
        DuruByte errorBuffer[errorBufferSize];                                 \
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

DuruByte* duruGetCwdName();

void  duruCreateArena();
void  duruDestroyArena();
void  duruMarkArena();
void  duruClearArena();
void* duruAllocateArena(DuruSize size, DuruSize alignment);

void duruInitialize();

#endif // DURU_H
