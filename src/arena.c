#include "duru.h"

#include <stdlib.h>

typedef struct DuruArena  DuruArena;
typedef struct DuruBlock  DuruBlock;
typedef struct DuruBlocks DuruBlocks;
typedef struct DuruMark   DuruMark;
typedef struct DuruMarks  DuruMarks;

struct DuruBlock {
    DuruByte* bytes;
    DuruSize  capacity;
    DuruSize  size;
};

struct DuruBlocks {
    DuruBlock* elements;
    DuruSize   length;
    DuruSize   count;
};

struct DuruMark {
    DuruSize block;
    DuruSize size;
};

struct DuruMarks {
    DuruMark* elements;
    DuruSize  length;
    DuruSize  count;
};

struct DuruArena {
    DuruBlocks blocks;
    DuruSize   activeBlock;
    DuruMarks  marks;
};

const DuruSize duruByte            = 1;
const DuruSize duruKilobyte        = duruByte * 1024;
const DuruSize duruMegabyte        = duruKilobyte * 1024;
const DuruSize duruDefaultCapacity = duruMegabyte;

static DuruArena duruArena;

static void duruCreateBlock(DuruSize capacity);

void duruCreateArena() {
    {
        void* new = malloc(sizeof(DuruBlock));
        duruEnsure(new, "Could not allocate!");
        duruArena.blocks = (DuruBlocks){.elements = new, .length = 1};
    }
    {
        void* new = malloc(sizeof(DuruMark));
        duruEnsure(new, "Could not allocate!");
        duruArena.marks = (DuruMarks){.elements = new, .length = 1};
    }
    {
        void* new = malloc(duruDefaultCapacity);
        duruEnsure(new, "Could not allocate!");
        duruArena.blocks.elements[0] =
          (DuruBlock){.bytes = new, .capacity = duruDefaultCapacity};
    }
}

void duruDestroyArena() {
    for (DuruSize i = 0; i < duruArena.blocks.count; i++) {
        free(duruArena.blocks.elements[i].bytes);
    }
    free(duruArena.blocks.elements);
    free(duruArena.marks.elements);
}

void duruMarkArena() {
    if (duruArena.marks.count == duruArena.marks.length) {
        if (duruArena.marks.length == 0) {
            duruArena.marks.length = 1;
        } else {
            duruArena.marks.length *= 2;
        }
        duruArena.marks.elements = realloc(
          duruArena.marks.elements, duruArena.marks.length * sizeof(DuruMark));
        duruEnsure(duruArena.marks.elements, "Could not allocate!");
    }
    duruArena.marks.elements[duruArena.marks.count] = (DuruMark){
      .block = duruArena.activeBlock,
      .size  = duruArena.blocks.elements[duruArena.activeBlock].size};
    duruArena.marks.count++;
}

void duruClearArena() {
    DuruMark mark = duruArena.marks.elements[duruArena.marks.count];
    duruArena.marks.count--;
    duruArena.activeBlock                      = mark.block;
    duruArena.blocks.elements[mark.block].size = mark.size;
}

void* duruAllocateArena(DuruSize size, DuruSize alignment) {
    DuruSize activeSize = duruArena.blocks.elements[duruArena.activeBlock].size;
    DuruSize startIndex = activeSize + alignment - activeSize % alignment;
    DuruSize endIndex   = startIndex + size;
    if (endIndex <= duruArena.blocks.elements[duruArena.activeBlock].capacity) {
        duruArena.blocks.elements[duruArena.activeBlock].size = endIndex;
        return duruArena.blocks.elements[duruArena.activeBlock].bytes
             + startIndex;
    }
    DuruSize biggestEmptyBlock         = duruArena.blocks.count;
    DuruSize biggestEmptyBlockCapacity = 0;
    for (DuruSize i = duruArena.activeBlock + 1; i < duruArena.blocks.count;
         i++) {
        DuruSize capacity = duruArena.blocks.elements[i].capacity;
        if (capacity <= biggestEmptyBlockCapacity) { continue; }
        biggestEmptyBlock         = i;
        biggestEmptyBlockCapacity = capacity;
    }
    if (biggestEmptyBlock == duruArena.blocks.count) {
        if (duruDefaultCapacity < size) {
            duruCreateBlock(size);
        } else {
            duruCreateBlock(duruDefaultCapacity);
        }
    } else if (duruArena.blocks.elements[biggestEmptyBlock].capacity < size) {
        duruCreateBlock(size);
    }
    duruArena.activeBlock++;
    if (duruArena.activeBlock != biggestEmptyBlock) {
        DuruBlock activeSlotHolder =
          duruArena.blocks.elements[duruArena.activeBlock];
        duruArena.blocks.elements[duruArena.activeBlock] =
          duruArena.blocks.elements[biggestEmptyBlock];
        duruArena.blocks.elements[biggestEmptyBlock] = activeSlotHolder;
    }
    duruArena.blocks.elements[duruArena.activeBlock].size = size;
    return duruArena.blocks.elements[duruArena.activeBlock].bytes;
}

static void duruCreateBlock(DuruSize capacity) {
    if (duruArena.blocks.count == duruArena.blocks.length) {
        duruArena.blocks.length *= 2;
        duruArena.blocks.elements =
          realloc(duruArena.blocks.elements, duruArena.blocks.length);
        duruEnsure(duruArena.blocks.elements, "Could not allocate!");
    }
    void* new = malloc(capacity);
    duruEnsure(new, "Could not allocate!");
    duruArena.blocks.elements[duruArena.blocks.count] =
      (DuruBlock){.bytes = new, .capacity = capacity};
    duruArena.blocks.count++;
}
