#include "internal.h"

#include <stddef.h>
#include <stdio.h>
#include <windows.h>

static DuruString duruTerminatePath(DuruStringView path);
static void       duruClearDirectory(DuruStringView path);

#define duruLoadChunkSize 1024

DuruStringView duruGetFileName(DuruStringView path) {
    for (size_t i = path.size; i; i--) {
        if (path.bytes[i - 1] == '\\' || path.bytes[i - 1] == '/') {
            return duruRemoveLeading(path, i);
        }
    }
    return path;
}

void duruAppendCwd(DuruString* string) {
    duruReserveBytes(string, MAX_PATH);
    size_t cwd = GetCurrentDirectory(MAX_PATH, string->bytes + string->size);
    duruEnsure(cwd, "Could not get the current working directory!");
    string->size += cwd;
}

void duruLoadFile(DuruStringView path, DuruString* contents) {
    DuruString terminatedPath = duruTerminatePath(path);
    FILE*      file           = fopen(terminatedPath.bytes, "r");
    duruDestroyString(terminatedPath);
    duruEnsure(
      file, "Could not open the file `%.*s`!", (int)path.size, path.bytes);
    size_t read;
    do {
        duruReserveBytes(contents, duruLoadChunkSize);
        read =
          fread(contents->bytes + contents->size, 1, duruLoadChunkSize, file);
        contents->size += read;
    } while (read == duruLoadChunkSize);
    duruEnsure(
      feof(file),
      "Could not read the file `%.*s`!",
      (int)path.size,
      path.bytes);
    duruEnsure(
      !fclose(file),
      "Could not close the file `%.*s`!",
      (int)path.size,
      path.bytes);
}

void duruStoreFile(DuruStringView path, DuruStringView contents) {
    DuruString terminatedPath = duruTerminatePath(path);
    FILE*      file           = fopen(terminatedPath.bytes, "wx");
    duruDestroyString(terminatedPath);
    duruEnsure(
      file, "Could not open the file `%.*s`!", (int)path.size, path.bytes);
    duruEnsure(
      fwrite(contents.bytes, 1, contents.size, file) == contents.size,
      "Could not write to the file `%.*s`!",
      (int)path.size,
      path.bytes);
    duruEnsure(
      !fclose(file),
      "Could not close the file `%.*s`!",
      (int)path.size,
      path.bytes);
}

void duruEnter(DuruStringView path) {
    DuruString terminatedPath = duruTerminatePath(path);
    if (SetCurrentDirectory(terminatedPath.bytes)) {
        duruDestroyString(terminatedPath);
        return;
    }
    duruDestroyString(terminatedPath);
    duruFail(
      "Could not set the current working directory to `%.*s`!",
      (int)path.size,
      path.bytes);
}

void duruEnsureDirectory(DuruStringView path) {
    DuruString terminatedPath = duruTerminatePath(path);
    if (CreateDirectory(terminatedPath.bytes, 0)) {
        duruDestroyString(terminatedPath);
        return;
    }
    duruDestroyString(terminatedPath);
    if (GetLastError() == ERROR_ALREADY_EXISTS) { return; }
    duruFail(
      "Could not create a directory at `%.*s`!", (int)path.size, path.bytes);
}

void duruRecreateDirectory(DuruStringView path) {
    DuruString terminatedPath = duruTerminatePath(path);
    if (CreateDirectory(terminatedPath.bytes, 0)) {
        duruDestroyString(terminatedPath);
        return;
    }
    duruDestroyString(terminatedPath);
    duruEnsure(
      GetLastError() == ERROR_ALREADY_EXISTS,
      "Could not create a directory at `%.*s`!",
      (int)path.size,
      path.bytes);
    duruClearDirectory(path);
}

static DuruString duruTerminatePath(DuruStringView path) {
    DuruString terminatedPath = {};
    duruAppend(&terminatedPath, path);
    duruTerminateString(&terminatedPath);
    return terminatedPath;
}

static void duruClearDirectory(DuruStringView path) {
    WIN32_FIND_DATA entry;
    DuruString      searchString = {};
    duruAppend(&searchString, path);
    duruAppend(&searchString, duruView("\\*"));
    duruTerminateString(&searchString);
    HANDLE search = FindFirstFile(searchString.bytes, &entry);
    duruDestroyString(searchString);
    duruEnsure(
      search != INVALID_HANDLE_VALUE,
      "Could not iterate the entries of the directory `%.*s`!",
      (int)path.size,
      path.bytes);
    do {
        if (strcmp(entry.cFileName, ".") == 0) { continue; }
        if (strcmp(entry.cFileName, "..") == 0) { continue; }
        DuruString entryPath = {};
        duruAppend(&entryPath, path);
        duruAppend(&entryPath, duruView("\\"));
        duruAppend(&entryPath, duruView(entry.cFileName));
        duruTerminateString(&entryPath);
        if (entry.dwFileAttributes & FILE_ATTRIBUTE_DIRECTORY) {
            duruClearDirectory(duruViewString(entryPath));
            duruEnsure(
              RemoveDirectory(entryPath.bytes),
              "Could not delete the directory `%s`!",
              entryPath.bytes);
        } else {
            duruEnsure(
              DeleteFile(entryPath.bytes),
              "Could not delete the file `%s`!",
              entryPath.bytes);
        }
        duruDestroyString(entryPath);
    } while (FindNextFile(search, &entry));
    duruEnsure(
      GetLastError() == ERROR_NO_MORE_FILES,
      "Could not iterate the entries of the directory `%.*s`!",
      (int)path.size,
      path.bytes);
    FindClose(search);
}
