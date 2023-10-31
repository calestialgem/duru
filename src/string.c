// Provides string utilities.

#include "duru.h"

#include <stdlib.h>
#include <string.h>

char* duruJoin(char const* prefix, char const* suffix) {
    size_t prefixLength = strlen(prefix);
    size_t suffixLength = strlen(suffix);
    size_t length       = prefixLength + suffixLength;
    char*  result       = malloc(length + 1);
    if (result == NULL) {
        duruCrash(
          "Could not allocate to join `%s` and `%s`; result was `%zu` bytes!",
          prefix,
          suffix,
          length + 1);
    }
    memcpy(result, prefix, prefixLength);
    memcpy(result + prefixLength, suffix, suffixLength);
    result[prefixLength + suffixLength] = 0;
    return result;
}
