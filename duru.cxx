#include <cstdio>
#include <cstdlib>

namespace duru {
  struct Unit {};

  template<typename Value, typename Error>
  struct Result {};

  struct Arena {};

  struct String {};

  struct InitializationError {};

  Result<Unit, InitializationError> initialize(
      Arena& temporary, String directory, String name) {
    return {};
  }
}

int main() {
  std::puts("Hello, World!");
  return EXIT_SUCCESS;
}
