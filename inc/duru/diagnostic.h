// Reports and propagates failures.

#pragma once

/// Information about an failure.
typedef struct DuruDiagnostic DuruDiagnostic;

/// Staticly allocated information of a source file in the program.
typedef struct DuruLocation DuruLocation;

/// Creates a diagnostic originated at the given source location with the given
/// formatted message.
///
/// Use `duruFail` instead of manually creating and returning diagnostics.
[[gnu::format(printf, 2, 3)]] DuruDiagnostic duruCreateDiagnostic(
  DuruLocation location, char const* format, ...);

/// See `duruAbortOnFailure`.
void duruAbortOnFailureWithLocation(
  DuruLocation location, DuruDiagnostic diagnostic);

/// Location of the invoker.
#define duruHere()                                                             \
    ((DuruLocation){.function = __func__, .file = __FILE__, .line = __LINE__})

/// Returns a diagnostic with the given message.
#define duruFail(format, ...)                                                  \
    do {                                                                       \
        return duruCreateDiagnostic(duruHere(), format, __VA_ARGS__);          \
    } while (0)

/// Checks the given diagnostic. Reports and aborts if there was a failure.
#define duruAbortOnFailure(diagnostic)                                         \
    duruAbortOnFailureWithLocation(duruHere(), diagnostic)

/// Returns a diagnostic that indicates that an unimplemented code path is
/// entered.
#define duruUnimplemented() duruFail("Unimplemented yet!")

struct DuruLocation {
    char const* function;
    char const* file;
    unsigned    line;
};

struct DuruDiagnostic {
    char* bytes;
    int   size;
    int   length;
};
