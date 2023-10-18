#include "internal.h"

#define duruTestDirectory               "tests"
#define duruInitializationTestDirectory "initTest"

static void duruTestInitialization();

int main() {
    duruEnsureDirectory(duruTestDirectory);
    duruEnter(duruTestDirectory);
    duruTestInitialization();
}

static void duruTestInitialization() {
    // duruRecreateDirectory(duruInitializationTestDirectory);
    duruEnter(duruInitializationTestDirectory);
    duruInitialize();
}
