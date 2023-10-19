#include "internal.h"

#include <stddef.h>
#include <stdlib.h>

static void duruReserveStrings(DuruStringList* list, size_t amount);

void duruDestroyStringList(DuruStringList list) { free(list.elements); }

void duruPushString(DuruStringList* list, DuruStringView string) {
    duruReserveStrings(list, 1);
    list->elements[list->count++] = string;
}

static void duruReserveStrings(DuruStringList* list, size_t amount) {
    size_t space = list->length - list->count;
    if (space >= amount) { return; }
    size_t required = amount - space;
    size_t growth   = required;
    if (growth < list->length) { growth = list->length; }
    list->length   += growth;
    list->elements  = realloc(list->elements, list->length);
    duruEnsure(
      list->elements,
      "Could not grow a string list from %zu strings to %zu strings!",
      list->count + space,
      list->length);
}
