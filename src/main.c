#include "duru.h"

#include <stdio.h>
#include <string.h>

int main() {
    DuruArena arena;
    duruDoOrDie(duruCreateArena, &arena);
    char const* staticMessage0 = "Repeat with me; ";
    char const* staticMessage1 = "Hello, Duru!";
    DuruSize    messageLength =
      (DuruSize)strlen(staticMessage0) + (DuruSize)strlen(staticMessage1) + 1;
    char* message;
    duruDoOrDie(duruAllocateArray, &arena, char, messageLength, &message);
    memcpy(message, staticMessage0, messageLength);
    memcpy(message + strlen(staticMessage0), staticMessage1, messageLength);
    message[messageLength] = 0;
    puts(message);
    duruDoOrDie(duruDestroyArena, &arena);
}
