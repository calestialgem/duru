package duru;

import java.nio.file.Path;

public final class Compiler {
  public static Semantic.Target compile(Path directory, Path libraries) {
    var compiler = new Compiler(directory, libraries);
    return compiler.compile();
  }

  private final Path                            directory;
  private final Path                            libraries;
  private String                                main;
  private AcyclicCache<String, Semantic.Module> modules;

  private Compiler(Path directory, Path libraries) {
    this.directory = directory;
    this.libraries = libraries;
  }

  private Semantic.Target compile() {
    main    = directory.getFileName().toString();
    modules = AcyclicCache.create(this::checkModule);
    modules.get(main);
    return new Semantic.Target(main, modules.getAll());
  }

  private Semantic.Module checkModule(String name) {
    if (name.equals(main))
      return checkModule(directory);
    return checkModule(libraries.resolve(name));
  }

  private Semantic.Module checkModule(Path directory) {
    return ModuleChecker.check(directory);
  }
}
