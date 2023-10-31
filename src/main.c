// Contains the tool's entrypoint.

#include "duru.h"

#include <locale.h>
#include <stdlib.h>

// Tests initializing a project.
static void testInitialization();

// Tests building a project.
static void testBuilding();

// Enters to the tool.
int main() {
    if (setlocale(LC_ALL, "en_US.utf8") == NULL) {
        duruCrash("Could not set the locale!");
    }
    testInitialization();
    testBuilding();
}

static void testInitialization() {
    // Path to the test projects' directory.
    char const testDirectory[] = "initTest";

    duruRecreateDirectory(testDirectory);
    char* oldCwd = duruGetCurrentDirectory();
    duruEnterDirectory(testDirectory);

    duruInitialize();

    duruEnterDirectory(oldCwd);
    free(oldCwd);
}

static void testBuilding() {
    // Path to the test projects' directory.
    char const testDirectory[] = "walkthrough";

    char* oldCwd = duruGetCurrentDirectory();
    duruEnterDirectory(testDirectory);

    duruBuild();

    duruEnterDirectory(oldCwd);
    free(oldCwd);
}
