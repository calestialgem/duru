#pragma once

#include <stdbool.h>

typedef struct DuruString DuruString;

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

char*      duruJoin(char const* prefix, char const* body, char const* suffix);
DuruString duruLoadFile(char const* path);
void       duruDestroyString(DuruString string);

char* duruGetCwd();
char* duruGetFileName(char const* path);
void  duruEnter(char const* path);
void  duruEnsureDirectory(char const* path);
void  duruRecreateDirectory(char const* path);

void duruInitialize();
void duruCompile();

#define duruPackageFile "package.duru"

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
