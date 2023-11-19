package duru;

import java.nio.file.Path;

public final class ModuleChecker {
  public static Semantic.Module check(Path directory) {
    var checker = new ModuleChecker(directory);
    return checker.check();
  }

  private final Path                             directory;
  private String                                 name;
  private Path                                   sources;
  private Path                                   artifacts;
  private Configuration                          configuration;
  private AcyclicCache<String, Semantic.Package> packages;

  private ModuleChecker(Path directory) {
    this.directory = directory;
  }

  private Semantic.Module check() {
    name      = directory.getFileName().toString();
    sources   = directory.resolve("src");
    artifacts = directory.resolve("art");
    Persistance.ensure(artifacts);
    resolveConfiguration();
    packages = AcyclicCache.create(this::checkPackage);
    checkPackageDeclarations();
    return new Semantic.Module(name, packages.getAll());
  }

  private void checkPackageDeclarations() {
    for (var executable : configuration.executables()) {
      var main = packages.get(executable).symbols().get("main");
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
      var main = packages.get(library).symbols().get("main");
      if (!main.isEmpty() && main.getFirst() instanceof Semantic.Proc)
        throw Subject
          .error("library package `%s` has a main procedure", library);
    }
  }

  private void resolveConfiguration() {
    var configurationFile   = directory.resolve("module.duru");
    var configurationSource =
      new Source(configurationFile, Persistance.load(configurationFile));
    Persistance.record(artifacts, configurationSource, "module", "source");
    var configurationTokens = ConfigurationLexer.lex(configurationSource);
    Persistance.record(artifacts, configurationTokens, "module", "tokens");
    var configurationNode = ConfigurationParser.parse(configurationTokens);
    Persistance.record(artifacts, configurationNode, "module", "node");
    configuration = ConfigurationResolver.resolve(configurationNode);
    Persistance.record(artifacts, configuration, "module", "resolution");
  }

  private Semantic.Package checkPackage(String packageName) {
    PackageType type;
    if (configuration.executables().contains(packageName))
      type = PackageType.EXECUTABLE;
    else if (configuration.libraries().contains(packageName))
      type = PackageType.LIBRARY;
    else
      type = PackageType.IMPLEMENTATION;
    return PackageChecker.check(sources, artifacts, type, packageName);
  }
}
