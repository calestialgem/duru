package duru;

import java.nio.file.Path;

final class Launcher {
  public static void main(String[] arguments) {
    var initTestDirectory = NormalPath.of(Path.of("initTest"));
    Persistance
      .recreate(initTestDirectory)
      .then(v -> Initializer.initialize(initTestDirectory))
      .orThrow();
  }

  private Launcher() {}
}
