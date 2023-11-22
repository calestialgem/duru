package duru;

import java.nio.file.Path;

final class Launcher {
  public static void main(String[] arguments) {
    if (arguments.length == 0) {
      printUsage();
    }
    var launcher = new Launcher(List.of(arguments));
    launcher.launch();
  }

  private static void printUsage() {
    System.err.print("""
Usage: duru [flags] <command> <arguments>

Commands:

duru help
      Shows this message.

duru init
      Initializes a module in launched module path.

duru check
      Checks the launched module.

duru build
      Builds the launched module.

duru run <passed-arguments>
      Runs an executable package from the launched module. If a package is
    provided runs that. Otherwise runs the executable package in the module if
    there is only one such package.

duru testCompilerByDeletingWholeModulePath
      DELETES the launched module path. Initializes a module in the cleared
    module path. Then, builds it.
      Used for testing the compiler.

Flags:

  Flags can be present anywhere in the command line. Boolean flags do take have
values. String flags must have a value as an argument after the flag or as
juxtaposed to the flag with a separating `=`.
  Some flags have shortcuts.
  When the same flag is provided multiple times, it overrides the old value or
builds up a list depending on the flag.

--module, -m as String
      Picks an absolute or relative path as the module directory to launch in.
      Defaults to the current working directory.

--package, -p as String
      Picks a package from the module. Used by the `run` command.

--include, -I as String List
      Adds the given directory into the list of module bases. When the checked
    module accesses another module, it is looked from the given module bases. A
    module base is a module directory or a directory containing module
    directories.
      Can be used more than once to build up a list.
      Defaults to an empty list.

--debug, -d as Boolean
      Launches the compiler in debug mode. In debug mode, the compiler records
    the various representations of the program in the artifact directories of
    respective modules.
      Defaults to false.
""");
    System.exit(-1);
  }

  private final List<String> arguments;

  private Launcher(List<String> arguments) {
    this.arguments = arguments;
  }

  private void launch() {
    var modulePath      = Persistance.path(".");
    var packageName     = Optional.<String>absent();
    var moduleBases     = ListBuffer.<Path>create();
    var debugger        = CompilerDebugger.inactive();
    var command         = Optional.<String>absent();
    var passedArguments = ListBuffer.<String>create();
    for (var i = 0; i != arguments.length(); i++) {
      var argument = arguments.get(i);
      if (argument.startsWith("-")) {
        var separator = argument.indexOf('=');
        var flag      = argument;
        if (separator != -1) {
          flag = argument.substring(0, separator);
        }
        switch (flag) {
          case "--module", "-m" -> {
            if (separator != -1) {
              modulePath = Persistance.path(argument.substring(separator + 1));
            }
            else {
              i++;
              if (i == arguments.length()) {
                throw Diagnostic
                  .error("", "expected module path after `%s`", flag);
              }
              modulePath = Persistance.path(arguments.get(i));
            }
          }
          case "--package", "-p" -> {
            if (separator != -1) {
              packageName = Optional.present(argument.substring(separator + 1));
            }
            else {
              i++;
              if (i == arguments.length()) {
                throw Diagnostic
                  .error("", "expected package name after `%s`", flag);
              }
              packageName = Optional.present(arguments.get(i));
            }
          }
          case "--include", "-I" -> {
            if (separator != -1) {
              moduleBases
                .add(Persistance.path(argument.substring(separator + 1)));
            }
            else {
              i++;
              if (i == arguments.length()) {
                throw Diagnostic
                  .error("", "expected module base after `%s`", flag);
              }
              moduleBases.add(Persistance.path(arguments.get(i)));
            }
          }
          case "--debug", "-d" -> {
            if (separator != -1) {
              throw Diagnostic.error("", "`%s` does not take value", flag);
            }
            debugger = CompilerDebugger.active();
          }
          default -> throw Diagnostic.error("", "unknown flag `%s`", flag);
        }
        continue;
      }
      if (command.isEmpty()) {
        command = Optional.present(argument);
        continue;
      }
      passedArguments.add(argument);
    }
    if (command.isEmpty()) {
      throw Diagnostic.error("", "there is no command");
    }
    switch (command.getFirst()) {
      case "help" -> printUsage();
      case "init" -> init(modulePath);
      case "check" -> check(debugger, modulePath, moduleBases.toList());
      case "build" -> build(debugger, modulePath, moduleBases.toList());
      case "run" ->
        run(
          debugger,
          modulePath,
          moduleBases.toList(),
          packageName,
          passedArguments.toList());
      case "testCompilerByDeletingWholeModulePath" ->
        testCompilerByDeletingWholeModulePath(
          debugger,
          modulePath,
          moduleBases.toList());
      default ->
        throw Diagnostic.error("", "unknown command `%s`", command.getFirst());
    }
  }

  private void init(Path modulePath) {
    Initializer.initialize(modulePath);
  }

  private void check(
    CompilerDebugger debugger,
    Path modulePath,
    List<Path> moduleBases)
  {
    Checker.check(debugger, "", modulePath, moduleBases);
  }

  private void build(
    CompilerDebugger debugger,
    Path modulePath,
    List<Path> moduleBases)
  {
    var artifacts = modulePath.resolve("art");
    var target    = Checker.check(debugger, "", modulePath, moduleBases);
    Builder.build("", artifacts, target);
  }

  private void run(
    CompilerDebugger debugger,
    Path modulePath,
    List<Path> moduleBases,
    Optional<String> packageName,
    List<String> passedArguments)
  {
    var artifacts = modulePath.resolve("art");
    var target    = Checker.check(debugger, "", modulePath, moduleBases);
    Builder.build("", artifacts, target);
    var    module = target.modules().get(target.main()).getFirst();
    String name;
    if (!packageName.isEmpty()) {
      name = packageName.getFirst();
    }
    else {
      var executables = ListBuffer.<Name>create();
      for (var package_ : module.packages().values()) {
        if (package_ instanceof Semantic.Executable executable) {
          executables.add(executable.name());
        }
      }
      if (executables.length() > 1) {
        throw Diagnostic
          .error("", "which executable out of `%s`", executables.toList());
      }
      if (executables.isEmpty()) {
        throw Diagnostic.error("", "no executable in `%s`", module.name());
      }
      name = executables.getFirst().joined(".");
    }
    var binary   = artifacts.resolve("%s.exe".formatted(name));
    var exitCode = Processes.execute("", true, binary, passedArguments);
    if (exitCode != 0) {
      System.err.printf("note: `%s` exited with %d%n", binary, exitCode);
    }
  }

  private void testCompilerByDeletingWholeModulePath(
    CompilerDebugger debugger,
    Path modulePath,
    List<Path> moduleBases)
  {
    Persistance.recreate("", modulePath);
    Initializer.initialize(modulePath);
    var target = Checker.check(debugger, "", modulePath, moduleBases);
    Builder.build("", modulePath.resolve("art"), target);
  }
}
