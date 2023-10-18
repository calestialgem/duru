#include "internal.h"

#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <windows.h>

static void duruClearDirectory(char const* path);

char* duruGetCwd() {
    size_t cwdSize = GetCurrentDirectory(0, 0);
    char*  cwd     = malloc(cwdSize);
    duruEnsure(
            GetCurrentDirectory(cwdSize, cwd),
            "Could not get the current working directory!");
    return cwd;
}

char* duruGetFileName(char const* path) {
    size_t pathSize = strlen(path);
    size_t nameIndex;
    for (nameIndex = pathSize; nameIndex; nameIndex--) {
        if (path[nameIndex - 1] == '\\' || path[nameIndex - 1] == '/') {
            break;
        }
    }
    size_t nameSize = pathSize - nameIndex;
    char*  name     = malloc(nameSize + 1);
    strcpy(name, path + nameIndex);
    return name;
}

void duruEnter(char const* path) {
    if (SetCurrentDirectory(path)) { return; }
    duruFail("Could not set the current working directory to `%s`!", path);
}

void duruEnsureDirectory(char const* path) {
    if (CreateDirectory(path, 0)) { return; }
    if (GetLastError() == ERROR_ALREADY_EXISTS) { return; }
    duruFail("Could not create a directory at `%s`!", path);
}

void duruRecreateDirectory(char const* path) {
    if (CreateDirectory(path, 0)) { return; }
    duruEnsure(
            GetLastError() == ERROR_ALREADY_EXISTS,
            "Could not create a directory at `%s`!",
            path);
    duruClearDirectory(path);
}

static void duruClearDirectory(char const* path) {
    char*           searchString = duruJoin("", path, "\\*");
    WIN32_FIND_DATA entry;
    HANDLE          search = FindFirstFile(searchString, &entry);
    duruEnsure(
            search != INVALID_HANDLE_VALUE,
            "Could not iterate the entries of the directory `%s`!",
            path);
    do {
        if (strcmp(entry.cFileName, ".") == 0) { continue; }
        if (strcmp(entry.cFileName, "..") == 0) { continue; }
        char* entryPath = duruJoin(path, "\\", entry.cFileName);
        if (entry.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) {
            duruClearDirectory(entryPath);
            duruEnsure(
                    RemoveDirectory(entryPath),
                    "Could not delete the directory `%s`!",
                    entryPath);
        } else {
            duruEnsure(
                    DeleteFile(entryPath),
                    "Could not delete the file `%s`!",
                    entryPath);
        }
        free(entryPath);
    } while (FindNextFile(search, &entry));
    duruEnsure(
            GetLastError() == ERROR_NO_MORE_FILES,
            "Could not iterate the entries of the directory `%s`!",
            path);
    FindClose(search);
    free(searchString);
}
