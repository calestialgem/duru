package duru;

final class Launcher {
  public static void main(String[] arguments) {
    var directory = Persistance.path("testmodule");
    Persistance.recreate(directory);
    Initializer.initialize(directory);
    var semantics = Compiler.compile(directory);
    Builder.build(semantics);
  }

  private Launcher() {}
}
