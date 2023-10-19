#pragma once

#include <stdbool.h>
#include <stddef.h>

typedef struct DuruStringView DuruStringView;
typedef struct DuruString     DuruString;
typedef struct DuruStringList DuruStringList;

typedef struct DuruProjectConfiguration DuruProjectConfiguration;

struct DuruStringView {
    char const* bytes;
    size_t      size;
};

struct DuruString {
    char*  bytes;
    size_t capacity;
    size_t size;
};

struct DuruStringList {
    DuruStringView* elements;
    size_t          length;
    size_t          count;
};

struct DuruProjectConfiguration {
    DuruStringView name;
    DuruStringList executables;
    DuruStringList exports;
};

[[noreturn, gnu::format(printf, 4, 5)]] void duruCrash(
  char const* function,
  char const* file,
  unsigned    line,
  char const* format,
  ...);

DuruStringView duruView(char const* cString);
DuruStringView duruViewString(DuruString string);
DuruStringView duruRemoveLeading(DuruStringView string, size_t amount);
DuruStringView duruRemoveTrailing(DuruStringView string, size_t amount);
DuruStringView duruRemovePrefix(DuruStringView string, DuruStringView prefix);
DuruStringView duruRemoveSuffix(DuruStringView string, DuruStringView suffix);
int            duruCompare(DuruStringView this, DuruStringView that);
int            duruDecodeCharacter(DuruStringView string, size_t* byteIndex);

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

void duruDestroyStringList(DuruStringList list);
void duruPushString(DuruStringList* list, DuruStringView string);

void duruDestroyProjectConfiguration(DuruProjectConfiguration configuration);
void duruParseProjectConfiguration(
  DuruProjectConfiguration* configuration, DuruStringView contents);

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
