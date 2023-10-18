#pragma once

#include <stdbool.h>
#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

typedef struct DuruDiagnostic DuruDiagnostic;
typedef struct DuruArena      DuruArena;

struct DuruDiagnostic {
    char*     bytes;
    ptrdiff_t capacity;
    ptrdiff_t size;
};

struct DuruArena {
    char*     bytes;
    ptrdiff_t capacity;
    ptrdiff_t size;
};

DuruDiagnostic duruCreateArena(DuruArena* arena, ptrdiff_t capacity);
DuruDiagnostic duruDestroyArena(DuruArena* arena);
DuruDiagnostic duruAllocateFromArena(
  DuruArena* arena, ptrdiff_t size, ptrdiff_t alignment, void** address);
DuruDiagnostic duruCreateDiagnostic(
  char const* function, char const* file, int line, char const* format, ...)
  __attribute__((format(printf, 4, 5)));

#define duruComplete()                                                         \
    do { return (DuruDiagnostic){}; } while (false)

#define duruFail(duruFailFormat, ...)                                          \
    do {                                                                       \
        return duruCreateDiagnostic(                                           \
          __func__, __FILE__, __LINE__, duruFailFormat, __VA_ARGS__);          \
    } while (false)

#define duruEnsure(duruEnsureCondition, duruEnsureFormat, ...)                 \
    do {                                                                       \
        if (!(duruEnsureCondition)) {                                          \
            duruFail(duruEnsureFormat, __VA_ARGS__);                           \
        }                                                                      \
    } while (false)

#define duruAssert(duruAssertCondition)                                        \
    duruEnsure(                                                                \
      duruAssertCondition,                                                     \
      "Assertion `" #duruAssertCondition "` does not hold!")

#define duruEnforce(duruEnforceExpression)                                     \
    do {                                                                       \
        DuruDiagnostic duruEnforceDiagnostic = duruEnforceExpression;          \
        if (duruEnforceDiagnostic.bytes) {                                     \
            (void)fputs(duruEnforceDiagnostic.bytes, stderr);                  \
            free(duruEnforceDiagnostic.bytes);                                 \
            abort();                                                           \
        }                                                                      \
    } while (false)
