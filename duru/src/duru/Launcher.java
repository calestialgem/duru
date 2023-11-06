package duru;

import java.nio.file.Path;

final class Launcher {
  public static void main(String[] arguments) {
    Result
      .perform(() -> testInitialization())
      .perform(v -> testBuilding())
      .orThrow();
  }

  private static Result<Void> testInitialization() {
    var initTestDirectory = NormalPath.of(Path.of("initTest"));
    return Result
      .perform(() -> Persistance.recreate(initTestDirectory))
      .perform(v -> Initializer.initialize(initTestDirectory));
  }

  private static Result<Void> testBuilding() {
    return Result
      .perform(() -> Compiler.compile(NormalPath.of(Path.of("walkthrough"))))
      .perform(target -> Builder.build(target));
  }

  private Launcher() {}
}
