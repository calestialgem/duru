#include "internal.h"

#include <stddef.h>
#include <stdlib.h>
#include <string.h>

void duruDestroyString(DuruString string) { free(string.bytes); }

void duruReserveBytes(DuruString* string, size_t amount) {
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

void duruTerminateString(DuruString* string) {
    duruReserveBytes(string, 1);
    string->bytes[string->size] = 0;
}

void duruAppend(DuruString* string, DuruStringView suffix) {
    duruReserveBytes(string, suffix.size);
    memcpy(string->bytes + string->size, suffix.bytes, suffix.size);
    string->size += suffix.size;
}
