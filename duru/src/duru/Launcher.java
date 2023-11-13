package duru;

final class Launcher {
  public static void main(String[] arguments) {
    testInitialization();
  }

  private static void testInitialization() {
    var testDirectory = Persistance.path("inittest");
    Persistance.recreate(testDirectory);
    Initializer.initialize(testDirectory);
  }

  private Launcher() {}
}
