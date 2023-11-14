package duru;

final class Launcher {
  public static void main(String[] arguments) {
    Subject.add("debug launcher");
    var directory = Persistance.path("testmodule");
    Subject.add(directory);
    Persistance.recreate(directory);
    Initializer.initialize(directory);
    var semantics = Compiler.compile(directory);
    Builder.build(semantics);
    Subject.remove();
    Subject.remove();
  }

  private Launcher() {}
}
