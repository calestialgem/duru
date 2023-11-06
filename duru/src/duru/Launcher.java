package duru;

import java.nio.file.Path;

final class Launcher {
  public static void main(String[] arguments) {
    Initializer.initialize(Path.of("initTest")).orThrow();
  }

  private Launcher() {}
}
