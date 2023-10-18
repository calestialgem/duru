#include "duru.h"

#include <stdio.h>
#include <string.h>

#define duruMessage "Hello, Duru!"

int main() {
    DuruArena arena;
    char*     message;
    int*      number;
    duruEnforce(duruCreateArena(&arena, 1024));
    duruEnforce(
      duruAllocateArray(&arena, char, strlen(duruMessage) + 1, &message));
    strcpy(message, duruMessage);
    duruEnforce(duruAllocate(&arena, int, &number));
    *number = 17;
    (void)fprintf(stderr, "%s %i\n", message, *number);
    duruEnforce(duruDestroyArena(&arena));
}
