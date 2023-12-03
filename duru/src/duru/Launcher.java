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

--explore, -e as Boolean
      Launches the compiler in exploration mode. In this mode, the compiler
    records the various representations of the program in the artifact
    directory of the launched module.
      Defaults to false.
""");
    System.exit(-1);
  }

  private final Checker checker;
  private final Builder builder;
  private final List<String> arguments;
  private Path modulePath;
  private List<Path> moduleBases;
  private Optional<String> packageName;
  private List<String> passedArguments;
  private Path artifacts;
  private Explorer explorer;

  private Launcher(List<String> arguments) {
    checker = new Checker();
    builder = new Builder();
    this.arguments = arguments;
  }

  private void launch() {
    modulePath = Persistance.path(".");
    packageName = Optional.absent();
    var moduleBases = ListBuffer.<Path>create();
    var explore = false;
    var command = Optional.<String>absent();
    var passedArguments = ListBuffer.<String>create();
    for (var i = 0; i != arguments.length(); i++) {
      var argument = arguments.get(i);
      if (argument.startsWith("-")) {
        var separator = argument.indexOf('=');
        var flag = argument;
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
          case "--explore", "-e" -> {
            if (separator != -1) {
              throw Diagnostic.error("", "`%s` does not take value", flag);
            }
            explore = true;
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
    this.moduleBases = moduleBases.toList();
    this.passedArguments = passedArguments.toList();
    artifacts = modulePath.resolve("art");
    explorer = new Explorer(artifacts, explore);
    switch (command.getFirst()) {
      case "help" -> printUsage();
      case "init" -> init();
      case "check" -> check();
      case "build" -> build();
      case "run" -> run();
      case "testCompilerByDeletingWholeModulePath" ->
        testCompilerByDeletingWholeModulePath();
      default ->
        throw Diagnostic.error("", "unknown command `%s`", command.getFirst());
    }
  }

  private void init() {
    Initializer.initialize(modulePath);
  }

  private void check() {
    checker.check(explorer, modulePath, moduleBases);
  }

  private void build() {
    var target = checker.check(explorer, modulePath, moduleBases);
    builder.build(artifacts, target);
  }

  private void run() {
    var target = checker.check(explorer, modulePath, moduleBases);
    builder.build(artifacts, target);
    String name;
    if (!packageName.isEmpty()) {
      name = packageName.getFirst();
    }
    else {
      throw Diagnostic.unimplemented(modulePath);
      // var module = target.modules().get(target.main()).getFirst();
      // var executables = ListBuffer.<Name>create();
      // for (var package_ : module.packages().values()) {
      // if (package_ instanceof Semantic.Executable executable) {
      // executables.add(executable.name());
      // }
      // }
      // if (executables.length() > 1) {
      // throw Diagnostic
      // .error("", "which executable out of `%s`", executables.toList());
      // }
      // if (executables.isEmpty()) {
      // throw Diagnostic.error("", "no executable in `%s`", module.name());
      // }
      // name = executables.getFirst().joined(".");
    }
    var binary = artifacts.resolve("%s.exe".formatted(name));
    var exitCode = Processes.execute("", true, binary, passedArguments);
    if (exitCode != 0) {
      System.err.printf("note: `%s` exited with %d%n", binary, exitCode);
    }
  }

  private void testCompilerByDeletingWholeModulePath() {
    Persistance.recreate("", modulePath);
    Initializer.initialize(modulePath);
    var target = checker.check(explorer, modulePath, moduleBases);
    builder.build(artifacts, target);
  }
}
