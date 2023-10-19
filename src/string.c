#include "internal.h"

#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#define duruLoadChunkSize 1024

static void duruReserveBytes(DuruString* string, size_t amount);

char* duruJoin(char const* prefix, char const* body, char const* suffix) {
    size_t prefixSize = strlen(prefix);
    size_t bodySize   = strlen(body);
    size_t suffixSize = strlen(suffix);
    char*  result     = malloc(prefixSize + bodySize + suffixSize + 1);
    duruEnsure(
      result,
      "Could not allocate %zu bytes to join strings!",
      prefixSize + bodySize + suffixSize + 1);
    strcpy(result, prefix);
    strcpy(result + prefixSize, body);
    strcpy(result + prefixSize + bodySize, suffix);
    return result;
}

DuruString duruLoadFile(char const* path) {
    FILE* file = fopen(path, "r");
    duruEnsure(file, "Could not open the file `%s`!", path);
    DuruString contents = {};
    size_t     read;
    do {
        duruReserveBytes(&contents, duruLoadChunkSize);
        read =
          fread(contents.bytes + contents.size, 1, duruLoadChunkSize, file);
        contents.size += read;
    } while (read == duruLoadChunkSize);
    duruReserveBytes(&contents, 1);
    contents.bytes[contents.size] = 0;
    duruEnsure(feof(file), "Could not read the file `%s`!", path);
    duruEnsure(!fclose(file), "Could not close the file `%s`!", path);
    return contents;
}

void duruDestroyString(DuruString string) { free(string.bytes); }

static void duruReserveBytes(DuruString* string, size_t amount) {
    size_t space = string->capacity - string->size;
    if (space >= amount) { return; }
    size_t required = amount - space;
    size_t growth   = required;
    if (growth < string->capacity) { growth = string->capacity; }
    string->capacity += growth;
    string->bytes     = realloc(string->bytes, string->capacity);
    duruEnsure(
      string->bytes,
      "Could not grow a string from %zu bytes to %zu bytes!",
      string->size + space,
      string->capacity);
}
