#ifndef DURU_H
#define DURU_H 1

#include <stdalign.h>
#include <stdbool.h>
#include <stddef.h>
#include <stdint.h>
#include <stdlib.h>

typedef uint8_t   DuruByte;
typedef ptrdiff_t DuruSize;

typedef struct DuruStatus DuruStatus;

struct [[nodiscard]] DuruStatus {
    char const* message;
    DuruStatus* cause;
};

#define duruIsNotOk(status) ((status).message)

#define duruOk()                                                               \
    do { return (DuruStatus){0}; } while (false)

#define duruThrow(messageText)                                                 \
    do { return (DuruStatus){.message = (messageText)}; } while (false)

#define duruExpect(condition, messageText)                                     \
    do {                                                                       \
        if (!(condition)) { duruThrow(messageText); }                          \
    } while (false)

#define duruTry(procedure, ...)                                                \
    do {                                                                       \
        DuruStatus status = procedure(__VA_ARGS__);                            \
        if (duruIsNotOk(status)) { return status; }                            \
    } while (false)

#define duruDoOrDie(procedure, ...)                                            \
    do {                                                                       \
        if (duruIsNotOk(procedure(__VA_ARGS__))) { exit(-1); }                 \
    } while (false)

typedef struct DuruArena      DuruArena;
typedef struct DuruArenaBlock DuruArenaBlock;

struct DuruArenaBlock {
    DuruByte* bytes;
    DuruSize  size;
};

struct DuruArena {
    DuruArena*     next;
    DuruArenaBlock block;
};

DuruStatus duruCreateArena(DuruArena* arena);
DuruStatus duruDestroyArena(DuruArena* arena);
DuruStatus duruAllocate(
  DuruArena* arena, DuruSize size, DuruSize alignment, void** block);

#define duruAllocateArray(arena, Element, length, block)                       \
    duruAllocate(                                                              \
      arena,                                                                   \
      (DuruSize)sizeof(Element) * (length),                                    \
      (DuruSize)alignof(Element),                                              \
      (void**)(block))
#define duruAllocateObject(arena, Object, block)                               \
    duruAllocateArray(arena, Object, 1, block)

#endif
