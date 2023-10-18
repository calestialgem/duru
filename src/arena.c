#include "duru.h"

#include <stddef.h>
#include <stdlib.h>

DuruDiagnostic duruCreateArena(DuruArena* arena, ptrdiff_t capacity) {
    duruAssert(arena);
    duruAssert(capacity >= 0);
    char* bytes = malloc(capacity);
    duruEnsure(bytes, "Could not allocate %zi bytes!", capacity);
    *arena = (DuruArena){.bytes = bytes, .capacity = capacity};
    duruComplete();
}

DuruDiagnostic duruDestroyArena(DuruArena* arena) {
    duruAssert(arena);
    duruAssert(arena->capacity >= 0);
    duruAssert(arena->size >= 0);
    duruAssert(arena->size <= arena->capacity);
    free(arena->bytes);
    duruComplete();
}

DuruDiagnostic duruAllocateFromArena(
  DuruArena* arena, ptrdiff_t size, ptrdiff_t alignment, void** address) {
    duruAssert(arena);
    duruAssert(arena->capacity >= 0);
    duruAssert(arena->size >= 0);
    duruAssert(arena->size <= arena->capacity);
    duruAssert(size >= 0);
    duruAssert(alignment >= 0);
    duruAssert(alignment <= (ptrdiff_t)alignof(max_align_t));
    duruAssert(address);
    ptrdiff_t overshoot = arena->size % alignment;
    ptrdiff_t padding   = 0;
    if (overshoot != 0) { padding = alignment - overshoot; }
    ptrdiff_t index = arena->size + padding;
    ptrdiff_t space = arena->capacity - index;
    duruEnsure(
      space >= size,
      "Could not allocate %zi bytes with %zi alignment from the arena that has %zi bytes of which %zi bytes are used!",
      size,
      alignment,
      arena->capacity,
      arena->size);
    *address    = arena->bytes + index;
    arena->size = index + size;
    duruComplete();
}
