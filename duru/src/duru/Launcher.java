package duru;

import java.nio.file.Path;

final class Launcher {
  public static void main(String[] arguments) {
    var initTestDirectory = NormalPath.of(Path.of("initTest"));
    Result
      .perform(() -> Persistance.recreate(initTestDirectory))
      .perform(v -> Initializer.initialize(initTestDirectory))
      .orThrow();
  }

  private Launcher() {}
}
