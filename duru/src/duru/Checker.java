package duru;

import java.nio.file.Path;

public final class Checker {
  public static Semantic.Target check(Path directory, Path libraries) {
    var checker = new Checker(directory, libraries);
    return checker.check();
  }

  private final Path                            directory;
  private final Path                            libraries;
  private String                                main;
  private AcyclicCache<String, Semantic.Module> modules;

  private Checker(Path directory, Path libraries) {
    this.directory = directory;
    this.libraries = libraries;
  }

  private Semantic.Target check() {
    main    = directory.getFileName().toString();
    modules = AcyclicCache.create(this::checkModule);
    modules.get(main);
    var target = new Semantic.Target(main, modules.getAll());
    Persistance.record(directory.resolve("art"), target, "target");
    return target;
  }

  private Semantic.Module checkModule(String name) {
    if (name.equals(main))
      return checkModule(directory);
    return checkModule(libraries.resolve(name));
  }

  private Semantic.Module checkModule(Path directory) {
    return ModuleChecker.check(this::accessModule, directory);
  }

  private Semantic.Module accessModule(String name) {
    return modules.get(name);
  }
}
