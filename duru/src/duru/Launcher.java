package duru;

final class Launcher {
  public static void main(String[] arguments) {
    Subject.add("debug launcher");
    var directory = Persistance.path("testmodule");
    Subject.add(directory);
    Persistance.recreate(directory);
    Initializer.initialize(directory);
    var target = Compiler.compile(directory);
    Builder.build(target);
    Subject.remove();
    Subject.remove();
  }

  private Launcher() {}
}
