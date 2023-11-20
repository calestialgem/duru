package duru;

final class Launcher {
  public static void main(String[] arguments) {
    if (arguments.length == 0) {
      printUsage();
    }
    var launcher =
      new Launcher(arguments[0], List.of(1, arguments.length - 1, arguments));
    launcher.launch();
  }

  private static void printUsage() {
    System.err.print("""
Usage: duru <command> <arguments>

duru help
      Shows this message.

duru init [module-path]
      Initializes a module in the current working directory or its child if a
    module path is given.

duru check
      Checks the module in the current working directory.

duru build
      Builds the module in the current working directory.

duru run [package-name]
      Runs a package from the module in the current working directory after
    building it. The package name can be omitted if the module declares only 1
    executable package.

duru testCompiler
      Runs the tests made for the compiler. For debugging the compiler.
""");
    System.exit(-1);
  }

  private final String       command;
  private final List<String> arguments;

  private Launcher(String command, List<String> arguments) {
    this.command   = command;
    this.arguments = arguments;
  }

  private void launch() {
    switch (command) {
      case "help" -> printUsage();
      case "init" -> init();
      case "check" -> check();
      case "build" -> build();
      case "run" -> run();
      case "testCompiler" -> testCompiler();
      default -> {
        System.err.printf("error: unknown command `%s`%n", command);
        printUsage();
      }
    }
  }

  private void init() {
    if (arguments.length() > 1) {
      System.err.printf("error: too many arguments `%s`%n", arguments);
      printUsage();
    }
    var modulePath = Persistance.path(".");
    for (var argument : arguments)
      modulePath = modulePath.resolve(argument);
    Initializer.initialize(modulePath);
  }

  private void check() {
    if (arguments.length() > 0) {
      System.err.printf("error: too many arguments `%s`%n", arguments);
      printUsage();
    }
    var debugger   = CompilerDebugger.inactive();
    var subject    = "launcher";
    var modulePath = Persistance.path(".");
    Checker.check(debugger, subject, modulePath, List.of());
  }

  private void build() {
    if (arguments.length() > 0) {
      System.err.printf("error: too many arguments `%s`%n", arguments);
      printUsage();
    }
    var debugger   = CompilerDebugger.inactive();
    var subject    = "launcher";
    var modulePath = Persistance.path(".");
    var artifacts  = modulePath.resolve("art");
    var target     = Checker.check(debugger, subject, modulePath, List.of());
    Builder.build(subject, artifacts, target);
  }

  private void run() {
    if (arguments.length() > 1) {
      System.err.printf("error: too many arguments `%s`%n", arguments);
      printUsage();
    }
    var debugger   = CompilerDebugger.inactive();
    var subject    = "launcher";
    var modulePath = Persistance.path(".");
    var artifacts  = modulePath.resolve("art");
    var target     = Checker.check(debugger, subject, modulePath, List.of());
    Builder.build(subject, artifacts, target);
    var    module = target.modules().get(target.main()).getFirst();
    String name;
    if (!arguments.isEmpty()) {
      name = arguments.getFirst();
    }
    else {
      var executables = ListBuffer.<String>create();
      for (var package_ : module.packages().values()) {
        if (package_ instanceof Semantic.Executable executable)
          executables.add(executable.name());
      }
      if (executables.length() > 1) {
        System.err.printf("error: which executable out of `%s`%n", executables);
        printUsage();
      }
      if (executables.isEmpty()) {
        System.err.printf("error: no executable in `%s`%n", module.name());
        printUsage();
      }
      name = executables.getFirst();
    }
    var filename = "%s.exe".formatted(name.replace('.', '/'));
    var binary   = artifacts.resolve(filename);
    var exitCode = Processes.execute(subject, true, binary);
    if (exitCode != 0) {
      System.err.printf("note: `%s` exited with %d%n", binary, exitCode);
    }
  }

  private void testCompiler() {
    if (arguments.length() > 0) {
      System.err.printf("error: too many arguments `%s`%n", arguments);
      printUsage();
    }
    var subject   = "debug launcher";
    var debugger  = CompilerDebugger.active();
    var directory = Persistance.path("testmodule");
    Persistance.recreate(subject, directory);
    Initializer.initialize(directory);
    var target =
      Checker
        .check(
          debugger,
          subject,
          directory,
          List.of(Persistance.path("libraries")));
    Builder.build(subject, directory.resolve("art"), target);
  }
}
