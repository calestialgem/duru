#include <cstdio>
#include <duru/initializer.hxx>

int main() {
  std::printf("Hello, World!\n");
  (void)duru::initialize({}, {});
}
