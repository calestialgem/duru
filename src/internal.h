#pragma once

char* duruJoin(char const* prefix, char const* body, char const* suffix);
char* duruGetCwd();
char* duruGetFileName(char const* path);
void  duruEnter(char const* path);
void  duruEnsureDirectory(char const* path);
void  duruRecreateDirectory(char const* path);
void  duruInitialize();
