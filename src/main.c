#include "duru.h"

#include <stdio.h>
#include <string.h>

#define duruMessage "Hello, Duru!"

int main() {
    DuruArena arena;
    char*     message;
    duruEnforce(duruCreateArena(&arena, 1024));
    duruEnforce(duruAllocateFromArena(
      &arena, strlen(duruMessage) + 1, 1, (void**)&message));
    strcpy(message, duruMessage);
    puts(message);
    duruEnforce(duruDestroyArena(&arena));
}
