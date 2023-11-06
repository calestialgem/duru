package duru;

import java.nio.file.Path;

final class Launcher {
  public static void main(String[] arguments) {
    Initializer.initialize(NormalPath.of(Path.of("initTest"))).orThrow();
  }

  private Launcher() {}
}
