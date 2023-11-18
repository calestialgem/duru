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
  private ListBuffer<Path>                                   directoryStack;
  private ListBuffer<AcyclicCache<String, Semantic.Package>> packageCacheStack;
  private ListBuffer<AcyclicCache<String, Semantic.Symbol>>  symbolCacheStack;

  private Compiler(Path directory, Path libraries) {
    this.directory = directory;
    this.libraries = libraries;
  }

  private Semantic.Target compile() {
    main              = directory.getFileName().toString();
    modules           = AcyclicCache.create(this::compileModule);
    moduleNameStack   = ListBuffer.create();
    directoryStack    = ListBuffer.create();
    packageCacheStack = ListBuffer.create();
    symbolCacheStack  = ListBuffer.create();
    modules.get(main);
    return new Semantic.Target(main, modules.getAll());
  }

  private Semantic.Module compileModule(String name) {
    if (name.equals(main))
      return compileModule(directory);
    return compileModule(libraries.resolve(name));
  }

  private Semantic.Module compileModule(Path directory) {
    directoryStack.add(directory);
    moduleNameStack.add(directory.getFileName().toString());
    packageCacheStack.add(AcyclicCache.create(this::compilePackage));
    var configuration = resolveConfiguration(directory.resolve("module.duru"));
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
    directoryStack.removeLast();
    return new Semantic.Module(
      moduleNameStack.removeLast(),
      packageCacheStack.removeLast().getAll());
  }

  private Semantic.Package getPackage(String name) {
    String moduleName;
    {
      var index = name.indexOf('.');
      if (index == -1) {
        moduleName = name;
      }
      else {
        moduleName = name.substring(index);
      }
    }
    if (!moduleName.equals(moduleNameStack.getLast())) {
      var package_ = modules.get(moduleName).packages().get(name);
      if (package_.isEmpty())
        throw Subject
          .error("there is no package `%s` in module `%s`", name, moduleName);
      if (!(package_.getFirst() instanceof Semantic.Library library))
        throw Subject
          .error(
            "package `%s` in module `%s` is not a library",
            name,
            moduleName);
      return library;
    }
    return packageCacheStack.getLast().get(name);
  }

  private Semantic.Package compilePackage(String name) {
    var sources   = directoryStack.getLast().resolve("src");
    var artifacts = directoryStack.getLast().resolve("art");
    var directory = packageDirectory(sources, name);
    symbolCacheStack.add(AcyclicCache.create(null));
    for (var file : Persistance.list(directory)) {
      var source       = new Source(file, Persistance.load(file));
      var fullFilename = file.getFileName().toString();
      var filename     =
        fullFilename.substring(0, fullFilename.length() - ".duru".length());
      record(artifacts, source, name, filename, "source");
    }
    throw Subject.unimplemented();
  }

  private Configuration resolveConfiguration(Path path) {
    var artifacts = directoryStack.getLast().resolve("art");
    Persistance.ensure(artifacts);
    var source = new Source(path, Persistance.load(path));
    record(artifacts, source, "module", "source");
    var tokens = ConfigurationLexer.lex(source);
    record(artifacts, tokens, "module", "tokens");
    var node = ConfigurationParser.parse(tokens);
    record(artifacts, node, "module", "node");
    var resolution = ConfigurationResolver.resolve(node);
    record(artifacts, resolution, "module", "resolution");
    return resolution;
  }

  private void record(Path artifacts, Object record, String... names) {
    var path = artifacts.resolve("%s.duru".formatted(String.join(".", names)));
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
