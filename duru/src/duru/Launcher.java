package duru;

final class Launcher {
  public static void main(String[] arguments) {
    var subject   = new Subject("debug launcher");
    var directory = Persistance.path(subject, "testmodule");
    Persistance.recreate(subject, directory);
    Initializer.initialize(subject, directory);
    var semantics = Compiler.compile(subject, directory);
    Builder.build(subject, semantics);
  }

  private Launcher() {}
}
