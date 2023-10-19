#include "internal.h"

#include <string.h>

typedef enum DuruTest DuruTest;

enum DuruTest {
    duruInitializationTest,
    duruErrorReportingTest,
    duruCompilingWalkthroughTest,
};

static void duruTestInitialization();
static void duruTestErrorReporting();
static void duruTestCompilingWalkthrough();

#define duruTestDirectory               duruView("tests")
#define duruInitializationTestDirectory duruView("initTest")
#define duruWalkthroughDirectory        duruView("walkthrough")

int main(int argumentCount, char const* arguments[const argumentCount]) {
    duruEnsure(argumentCount == 2, "Provide the test to be executed!");
    duruEnsure(
      strlen(arguments[1]) == 1,
      "The test indicator must be a single character!");
    DuruTest test;
    switch (arguments[1][0]) {
        case 'i': test = duruInitializationTest; break;
        case 'e': test = duruErrorReportingTest; break;
        case 'c': test = duruCompilingWalkthroughTest; break;
        default: duruFail("Unknown test indicator `%c`!", arguments[1][0]);
    }
    duruEnsureDirectory(duruTestDirectory);
    duruEnter(duruTestDirectory);
    switch (test) {
        case duruInitializationTest: duruTestInitialization(); break;
        case duruErrorReportingTest: duruTestErrorReporting(); break;
        case duruCompilingWalkthroughTest:
            duruTestCompilingWalkthrough();
            break;
        default: duruFail("Invalid enumerator `%i`!", test);
    }
}

static void duruTestInitialization() {
    duruRecreateDirectory(duruInitializationTestDirectory);
    duruEnter(duruInitializationTestDirectory);
    duruInitialize();
}

static void duruTestErrorReporting() {
    duruEnter(duruInitializationTestDirectory);
    duruInitialize();
}

static void duruTestCompilingWalkthrough() {
    duruEnter(duruWalkthroughDirectory);
    duruCompile();
}
