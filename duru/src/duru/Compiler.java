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
    modules = AcyclicCache.create(this::compileModule);
    modules.get(main);
    return new Semantic.Target(main, modules.getAll());
  }

  private Semantic.Module compileModule(String name) {
    if (name.equals(main))
      return compileModule(directory);
    return compileModule(libraries.resolve(name));
  }

  private Semantic.Module compileModule(Path directory) {
    return ModuleCompiler.compile(directory);
  }
}
