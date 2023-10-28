// Reports failures.

#pragma once

/// Use `duruFail` instead.
[[noreturn, gnu::format(printf, 4, 5)]] void duruFailWithLocation(
  char const* function,
  char const* file,
  unsigned    line,
  char const* format,
  ...);

/// Crashes the program with a message and a subject location.
#define duruFail(format, ...)                                                  \
    duruFailWithLocation(__func__, __FILE__, __LINE__, format, __VA_ARGS__)

/// Calls `duruFail` with a message explaining the feature is not implemented
/// yet.
#define duruUnimplemented() duruFail("Unimplemented!")
