package duru;

import java.nio.file.Path;

public final class Checker {
  public static Semantic.Target check(
    CompilerDebugger debugger,
    Object subject,
    Path directory,
    Path libraries)
  {
    var checker = new Checker(debugger, subject, directory, libraries);
    return checker.check();
  }

  private final CompilerDebugger                debugger;
  private final Object                          subject;
  private final Path                            directory;
  private final Path                            libraries;
  private String                                main;
  private AcyclicCache<String, Semantic.Module> modules;

  private Checker(
    CompilerDebugger debugger,
    Object subject,
    Path directory,
    Path libraries)
  {
    this.debugger  = debugger;
    this.subject   = subject;
    this.directory = directory;
    this.libraries = libraries;
  }

  private Semantic.Target check() {
    main    = directory.getFileName().toString();
    modules = AcyclicCache.create(this::checkModule);
    modules.get(subject, main);
    var target = new Semantic.Target(main, modules.getAll());
    debugger.recordTarget(directory.resolve("art"), target);
    return target;
  }

  private Semantic.Module checkModule(Object subject, String name) {
    if (name.equals(main))
      return checkModule(subject, directory);
    return checkModule(subject, libraries.resolve(name));
  }

  private Semantic.Module checkModule(Object subject, Path directory) {
    return ModuleChecker
      .check(debugger, subject, this::accessModule, directory);
  }

  private Semantic.Module accessModule(Object subject, String name) {
    return modules.get(subject, name);
  }
}
