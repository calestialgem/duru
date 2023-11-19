package duru;

final class Launcher {
  public static void main(String[] arguments) {
    var subject   = "debug launcher";
    var debugger  = CompilerDebugger.active();
    var directory = Persistance.path("testmodule");
    Persistance.recreate(subject, directory);
    Initializer.initialize(directory);
    var target =
      Checker
        .check(debugger, subject, directory, Persistance.path("libraries"));
    Builder.build(subject, target);
  }

  private Launcher() {}
}
