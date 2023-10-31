// Implements platform dependent directory operations.

#include "duru.h"

#include <stddef.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <wchar.h>
#include <windows.h>

// Deletes a directory.
static void removeDirectory(char const* path);

// Same as `duruCrashWithLocation` but takes the causes from the platform error
// system.
static void crashWithLocation(
  char const* function, char const* file, int line, char const* format, ...);

// Calls `crashWithLocation` with the invoker's location.
#define crash(format, ...)                                                     \
    crashWithLocation(__func__, __FILE__, __LINE__, format, __VA_ARGS__)

char const* duruGetFileName(char const* path) {
    for (size_t i = strlen(path) - 1; i != 0; i--) {
        if (path[i] == '\\' || path[i] == '/') { return path + i + 1; }
    }
    return path;
}

char* duruGetCurrentDirectory() {
    char* cwd = malloc(MAX_PATH);
    if (cwd == NULL) {
        duruCrash(
          "Could not allocate `%i` bytes for the current working directory!",
          MAX_PATH);
    }
    if (!GetCurrentDirectory(MAX_PATH, cwd)) {
        crash("Could not get the current working directory!");
    }
    return cwd;
}

void duruEnterDirectory(char const* path) {
    if (SetCurrentDirectory(path)) { return; }
    crash("Could not set the current working directory to `%s`!", path);
}

void duruEnsureDirectory(char const* path) {
    if (CreateDirectory(path, 0)) { return; }
    if (GetLastError() == ERROR_ALREADY_EXISTS) { return; }
    crash("Could not create a directory at `%s`!", path);
}

void duruRecreateDirectory(char const* path) {
    if (CreateDirectory(path, 0)) { return; }
    if (GetLastError() != ERROR_ALREADY_EXISTS) {
        crash("Could not create a directory at `%s`!", path);
    }
    removeDirectory(path);
    if (CreateDirectory(path, 0)) { return; }
    crash("Could not create a directory at `%s`!", path);
}

static void removeDirectory(char const* path) {
    WIN32_FIND_DATA data   = {0};
    HANDLE          handle = {0};
    {
        char* string = duruFormat("%s/*", path);
        handle       = FindFirstFile(string, &data);
        free(string);
    }
    if (handle == INVALID_HANDLE_VALUE) {
        crash("Could not list the entries of the directory at `%s`!", path);
    }

    do {
        if (strcmp(data.cFileName, ".") == 0) { continue; }
        if (strcmp(data.cFileName, "..") == 0) { continue; }
        char* entry = duruFormat("%s/%s", path, data.cFileName);
        if (data.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) {
            removeDirectory(entry);
        } else {
            if (!DeleteFile(entry)) {
                crash("Could not delete the file at `%s`!", entry);
            }
        }
        free(entry);
    } while (FindNextFile(handle, &data));

    if (GetLastError() != ERROR_NO_MORE_FILES) {
        crash(
          "Could not list the remaining entries of the directory at `%s`!",
          path);
    }

    FindClose(handle);
    if (!RemoveDirectory(path)) {
        crash("Could not delete the directory at `%s`!", path);
    }
}

static void crashWithLocation(
  char const* function, char const* file, int line, char const* format, ...) {
    DWORD  errorNumber = GetLastError();
    size_t suffix      = strlen(__FILE__) - strlen("platformWindows.c");
    (void)fprintf(
      stderr, "in %s at %s:%i\nfailure: ", function, file + suffix, line);
    va_list arguments = {0};
    va_start(arguments, format);
    if (vfprintf(stderr, format, arguments) < 0) {
        (void)fprintf(stderr, "Could not format the message `%s`!", format);
    }
    va_end(arguments);
    (void)fputc('\n', stderr);
    if (errorNumber) {
        LPWSTR buffer = {0};

        DWORD size = FormatMessageW(
          FORMAT_MESSAGE_ALLOCATE_BUFFER | FORMAT_MESSAGE_FROM_SYSTEM
            | FORMAT_MESSAGE_IGNORE_INSERTS,
          0,
          errorNumber,
          0,
          (LPWSTR)&buffer,
          0,
          0);

        if (size != 0) {
            (void)fwprintf(stderr, L"cause: %ls", buffer);
            LocalFree(buffer);
        } else {
            (void)fprintf(
              stderr,
              "cause: Could not get the system message for the cause; Windows error code was `0x%08lx`!",
              errorNumber);
        }
    }
    abort();
}
