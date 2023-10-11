#include "duru.h"

#include <stdlib.h>

const DuruSize arenaBlockCapacity = (DuruSize)1024 * (DuruSize)1024;

DuruStatus duruCreateArena(DuruArena* arena) {
    duruExpect(arena, "failure: Arena cannot be null!");
    arena->next        = 0;
    arena->block.bytes = 0;
    arena->block.size  = 0;
    duruOk();
}

DuruStatus duruDestroyArena(DuruArena* arena) {
    duruExpect(arena, "failure: Arena cannot be null!");
    if (arena->next) { duruTry(duruDestroyArena, arena->next); }
    free(arena->block.bytes);
    duruOk();
}

DuruStatus duruAllocate(
  DuruArena* arena, DuruSize size, DuruSize alignment, void** block) {
    duruExpect(arena, "failure: Arena cannot be null!");
    duruExpect(size >= 0, "failure: Size must be non-negative!");
    duruExpect(alignment > 0, "failure: Alignment must be positive!");
    duruExpect(block, "failure: Block cannot be null!");
    if (!arena->block.bytes) {
        void* newBytes = malloc(arenaBlockCapacity);
        duruExpect(newBytes, "failure: Could not allocate the initial arena!");
        arena->block.bytes = newBytes;
    }
    DuruSize alignedSize = arena->block.size + arena->block.size % alignment;
    DuruSize space       = arenaBlockCapacity - alignedSize;
    if (size > space) {
        duruTry(duruCreateArena, arena->next);
        return duruAllocate(arena->next, size, alignment, block);
    }
    arena->block.size = alignedSize + size;
    *block            = arena->block.bytes + alignedSize;
    duruOk();
}
