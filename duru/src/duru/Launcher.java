package duru;

import java.nio.file.Path;

final class Launcher {
  public static void main(String[] arguments) {
    testInitialization().orThrow();
  }

  private static Result<Void> testInitialization() {
    var initTestDirectory = NormalPath.of(Path.of("initTest"));
    return Result
      .perform(() -> Persistance.recreate(initTestDirectory))
      .perform(v -> Initializer.initialize(initTestDirectory));
  }

  private Launcher() {}
}
