package duru;

import java.nio.file.Path;

public final class ModuleChecker {
  public static Semantic.Module check(Path directory) {
    var checker = new ModuleChecker(directory);
    return checker.check();
  }

  private final Path directory;

  private ModuleChecker(Path directory) {
    this.directory = directory;
  }

  private Semantic.Module check() {
    var name      = directory.getFileName().toString();
    var sources   = directory.resolve("src");
    var artifacts = directory.resolve("art");
    var packages  =
      AcyclicCache.<String, Semantic
        .Package>create(
          packageName -> PackageChecker.check(sources, artifacts, packageName));
    Persistance.ensure(artifacts);
    var configurationFile   = directory.resolve("module.duru");
    var configurationSource =
      new Source(configurationFile, Persistance.load(configurationFile));
    Persistance.record(artifacts, configurationSource, "module", "source");
    var configurationTokens = ConfigurationLexer.lex(configurationSource);
    Persistance.record(artifacts, configurationTokens, "module", "tokens");
    var configurationNode = ConfigurationParser.parse(configurationTokens);
    Persistance.record(artifacts, configurationNode, "module", "node");
    var configuration1 = ConfigurationResolver.resolve(configurationNode);
    Persistance.record(artifacts, configuration1, "module", "resolution");
    var configuration = configuration1;
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
    return new Semantic.Module(name, packages.getAll());
  }
}
