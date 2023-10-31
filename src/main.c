// Contains the tool's entrypoint.

#include "duru.h"

#include <locale.h>

// Tests initializing a project.
static void testInitialization();

// Enters to the tool.
int main() {
    if (setlocale(LC_ALL, "en_US.utf8") == NULL) {
        duruCrash("Could not set the locale!");
    }
    testInitialization();
}

static void testInitialization() {
    // Path to the test projects' directory.
    char const testDirectory[] = "initTest";

    duruRecreateDirectory(testDirectory);
    duruEnterDirectory(testDirectory);

    duruInitialize();
}
