#include "internal.h"

#include <stdlib.h>
#include <string.h>

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
