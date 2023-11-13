package duru;

final class Launcher {
  public static void main(String[] arguments) {
    testInitialization();
    testBuilding();
  }

  private static void testInitialization() {
    var testDirectory = Persistance.path("inittest");
    Persistance.recreate(testDirectory);
    Initializer.initialize(testDirectory);
  }

  private static void testBuilding() {
    var testDirectory = Persistance.path("inittest");
    var target        = Compiler.compile(testDirectory);
    Builder.build(target);
  }

  private Launcher() {}
}
