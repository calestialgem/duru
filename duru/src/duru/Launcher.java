package duru;

final class Launcher {
  public static void main(String[] arguments) {
    Subject.add("debug launcher");
    var directory = Persistance.path("testmodule");
    Subject.add(directory);
    Persistance.recreate(directory);
    Initializer.initialize(directory);
    var target = Checker.check(directory, Persistance.path("libraries"));
    Builder.build(target);
    Subject.removeLast();
    Subject.removeLast();
  }

  private Launcher() {}
}
