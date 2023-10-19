#include "internal.h"

#include <stddef.h>
#include <string.h>

DuruStringView duruView(char const* cString) {
    return (DuruStringView){.bytes = cString, .size = strlen(cString)};
}

DuruStringView duruViewString(DuruString string) {
    return (DuruStringView){.bytes = string.bytes, .size = string.size};
}

DuruStringView duruRemoveLeading(DuruStringView string, size_t amount) {
    string.bytes += amount;
    string.size  -= amount;
    return string;
}

DuruStringView duruRemoveTrailing(DuruStringView string, size_t amount) {
    string.size -= amount;
    return string;
}

DuruStringView duruRemovePrefix(DuruStringView string, DuruStringView prefix) {
    if (
      string.size >= prefix.size
      && !memcmp(string.bytes, prefix.bytes, prefix.size)) {
        string = duruRemoveLeading(string, prefix.size);
    }
    return string;
}

DuruStringView duruRemoveSuffix(DuruStringView string, DuruStringView suffix) {
    if (
      string.size >= suffix.size
      && !memcmp(
        string.bytes + (string.size - suffix.size),
        suffix.bytes,
        suffix.size)) {
        string = duruRemoveTrailing(string, suffix.size);
    }
    return string;
}
