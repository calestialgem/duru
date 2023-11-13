package duru;

import java.nio.file.Path;

final class Launcher {
  public static void main(String[] arguments) {
    testInitialization();
  }

  private static void testInitialization() {
    var testDirectory = Path.of("inittest");
    Persistance.recreate(testDirectory);
    Initializer.initialize(testDirectory);
  }

  private Launcher() {}
}
