#include "internal.h"

#include <stddef.h>
#include <string.h>

#define duruMaxASCII 127

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

int duruCompare(DuruStringView this, DuruStringView that) {
    if (this.size < that.size) {
        int suffix = memcmp(this.bytes, that.bytes, this.size);
        if (suffix == 0) { return 1; }
        return suffix;
    }
    if (this.size > that.size) {
        int suffix = memcmp(this.bytes, that.bytes, that.size);
        if (suffix == 0) { return -1; }
        return suffix;
    }
    return memcmp(this.bytes, that.bytes, this.size);
}

int duruDecodeCharacter(DuruStringView string, size_t* byteIndex) {
    char byte0 = string.bytes[(*byteIndex)++];
    duruEnsure(byte0 <= duruMaxASCII, "UTF-8 is not implemented yet!");
    return (int)byte0;
}
