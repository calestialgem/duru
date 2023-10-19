#pragma once

#include <stdbool.h>

typedef struct DuruStringView DuruStringView;
typedef struct DuruString     DuruString;

struct DuruStringView {
    char const* bytes;
    size_t      size;
};

struct DuruString {
    char*  bytes;
    size_t capacity;
    size_t size;
};

[[noreturn]] void duruCrash(
  char const* function,
  char const* file,
  unsigned    line,
  char const* format,
  ...) __attribute__((format(printf, 4, 5)));

DuruStringView duruView(char const* cString);
DuruStringView duruViewString(DuruString string);
DuruStringView duruRemoveLeading(DuruStringView string, size_t amount);
DuruStringView duruRemoveTrailing(DuruStringView string, size_t amount);
DuruStringView duruRemovePrefix(DuruStringView string, DuruStringView prefix);
DuruStringView duruRemoveSuffix(DuruStringView string, DuruStringView suffix);

void duruDestroyString(DuruString string);
void duruReserveBytes(DuruString* string, size_t amount);
void duruTerminateString(DuruString* string);
void duruAppend(DuruString* string, DuruStringView suffix);

DuruStringView duruGetFileName(DuruStringView path);
void           duruAppendCwd(DuruString* string);
void           duruLoadFile(DuruStringView path, DuruString* contents);
void           duruStoreFile(DuruStringView path, DuruStringView contents);
void           duruEnter(DuruStringView path);
void           duruEnsureDirectory(DuruStringView path);
void           duruRecreateDirectory(DuruStringView path);

void duruInitialize();
void duruCompile();

#define duruProjectFile       duruView("project.duru")
#define duruSourceDirectory   duruView("src")
#define duruArtifactDirectory duruView("art")

#define duruFail(duruFailFormat, ...)                                          \
    do {                                                                       \
        duruCrash(__func__, __FILE__, __LINE__, duruFailFormat, __VA_ARGS__);  \
    } while (false)

#define duruEnsure(duruEnsureCondition, duruEnsureFormat, ...)                 \
    do {                                                                       \
        if (!(duruEnsureCondition)) {                                          \
            duruFail(duruEnsureFormat, __VA_ARGS__);                           \
        }                                                                      \
    } while (false)
