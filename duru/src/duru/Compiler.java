package duru;

import java.nio.file.Path;

public final class Compiler {
  public static Semantic.Target compile(Path directory, Path libraries) {
    var compiler = new Compiler(directory, libraries);
    return compiler.compile();
  }

  private final Path                                         directory;
  private final Path                                         libraries;
  private String                                             main;
  private AcyclicCache<String, Semantic.Module>              modules;
  private ListBuffer<String>                                 moduleNameStack;
  private ListBuffer<AcyclicCache<String, Semantic.Package>> packageCacheStack;

  private Compiler(Path directory, Path libraries) {
    this.directory = directory;
    this.libraries = libraries;
  }

  private Semantic.Target compile() {
    main              = directory.getFileName().toString();
    modules           = AcyclicCache.create(this::compileModule);
    moduleNameStack   = ListBuffer.create();
    packageCacheStack = ListBuffer.create();
    modules.get(main);
    return new Semantic.Target(main, modules.getAll());
  }

  private Semantic.Module compileModule(String name) {
    if (name.equals(main))
      return compileModule(directory);
    return compileModule(libraries.resolve(name));
  }

  private Semantic.Module compileModule(Path directory) {
    var configuration = resolveConfiguration(directory.resolve("module.duru"));
    moduleNameStack.add(directory.getFileName().toString());
    var sources = directory.resolve("src");
    packageCacheStack
      .add(
        AcyclicCache
          .create(name -> compilePackage(packageDirectory(sources, name))));
    for (var executable : configuration.executables()) {
      var main =
        packageCacheStack.getLast().get(executable).symbols().get("main");
      if (main.isEmpty() || !(main.getFirst() instanceof Semantic.Proc proc)) {
        throw Subject
          .error(
            "executable package `%s` does not have a main procedure",
            executable);
      }
      if (!proc.parameters().isEmpty())
        throw Subject
          .error(
            "executable package `%s` has a main procedure with parameters",
            executable);
      if (!proc.returnType().isEmpty())
        throw Subject
          .error(
            "executable package `%s` has a main procedure with a return type",
            executable);
    }
    for (var library : configuration.libraries()) {
      var main = packageCacheStack.getLast().get(library).symbols().get("main");
      if (!main.isEmpty() && main.getFirst() instanceof Semantic.Proc)
        throw Subject
          .error("library package `%s` has a main procedure", library);
    }
    return new Semantic.Module(
      moduleNameStack.removeLast(),
      packageCacheStack.removeLast().getAll(),
      configuration.executables(),
      configuration.libraries());
  }

  private Semantic.Package getPackage(String name) {
    String module;
    {
      var index = name.indexOf('.');
      if (index == -1) {
        module = name;
      }
      else {
        module = name.substring(index);
      }
    }
    if (!module.equals(moduleNameStack.getLast())) {
      return modules.get(module).packages().get(name).getFirst();
    }
    return packageCacheStack.getLast().get(name);
  }

  private Semantic.Package compilePackage(Path directory) {
    throw Subject.unimplemented();
  }

  private Configuration resolveConfiguration(Path path) {
    var artifacts = path.getParent().resolve("art");
    Persistance.ensure(artifacts);
    var source = new Source(path, Persistance.load(path));
    record(artifacts, "module.source", source);
    var tokens = ConfigurationLexer.lex(source);
    record(artifacts, "module.tokens", tokens);
    var node = ConfigurationParser.parse(tokens);
    record(artifacts, "module.node", node);
    var resolution = ConfigurationResolver.resolve(node);
    record(artifacts, "module.resolution", resolution);
    return resolution;
  }

  private void record(Path artifacts, Object name, Object record) {
    var path = artifacts.resolve("%s.duru".formatted(name));
    Persistance.store(path, record);
  }

  private Path packageDirectory(Path root, String name) {
    var directory = root;
    var index     = name.indexOf('.');
    while (index != -1) {
      var next = name.indexOf('.', index + 1);
      if (next == -1) {
        directory = directory.resolve(name.substring(index + 1));
      }
      else {
        directory = directory.resolve(name.substring(index + 1, next));
      }
      index = next;
    }
    return directory;
  }
}
