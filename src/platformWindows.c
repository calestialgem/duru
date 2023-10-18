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
    if (!GetCurrentDirectory(cwdSize, cwd)) {
        (void)fprintf(
                stderr,
                "failure: Could not get the current working directory!\n");
        abort();
    }
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
    (void)fprintf(
            stderr,
            "failure: Could not set the current working directory to `%s`!\n",
            path);
    abort();
}

void duruEnsureDirectory(char const* path) {
    if (CreateDirectory(path, 0)) { return; }
    if (GetLastError() == ERROR_ALREADY_EXISTS) { return; }
    (void)fprintf(
            stderr, "failure: Could not create a directory at `%s`!\n", path);
    abort();
}

void duruRecreateDirectory(char const* path) {
    if (CreateDirectory(path, 0)) { return; }
    if (GetLastError() != ERROR_ALREADY_EXISTS) {
        (void)fprintf(
                stderr,
                "failure: Could not create a directory at `%s`!\n",
                path);
        abort();
    }
    duruClearDirectory(path);
}

static void duruClearDirectory(char const* path) {
    char*           searchString = duruJoin("", path, "\\*");
    WIN32_FIND_DATA entry;
    HANDLE          search = FindFirstFile(searchString, &entry);
    if (search == INVALID_HANDLE_VALUE) {
        (void)fprintf(
                stderr,
                "failure: Could not iterate the entries of the directory `%s`!\n",
                path);
        abort();
    }
    do {
        if (strcmp(entry.cFileName, ".") == 0) { continue; }
        if (strcmp(entry.cFileName, "..") == 0) { continue; }
        char* entryPath = duruJoin(path, "\\", entry.cFileName);
        if (entry.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) {
            duruClearDirectory(entryPath);
            if (!RemoveDirectory(entryPath)) {
                (void)fprintf(
                        stderr,
                        "failure: Could not delete the directory `%s`!",
                        entryPath);
                abort();
            }
        } else {
            if (!DeleteFile(entryPath)) {
                (void)fprintf(
                        stderr,
                        "failure: Could not delete the file `%s`!",
                        entryPath);
                abort();
            }
        }
        free(entryPath);
    } while (FindNextFile(search, &entry));
    if (GetLastError() != ERROR_NO_MORE_FILES) {
        (void)fprintf(
                stderr,
                "failure: Could not iterate the entries of the directory `%s`!\n",
                path);
        abort();
    }
    FindClose(search);
    free(searchString);
}
