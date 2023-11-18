package duru;

import java.nio.file.Path;

public final class PackageChecker {
  public static Semantic.Package check(
    Path sources,
    Path artifacts,
    PackageType type,
    String name)
  {
    var checker = new PackageChecker(sources, artifacts, type, name);
    return checker.check();
  }

  private final Path                            sources;
  private final Path                            artifacts;
  private final PackageType                     type;
  private final String                          name;
  private AcyclicCache<String, Semantic.Symbol> symbols;

  private PackageChecker(
    Path sources,
    Path artifacts,
    PackageType type,
    String name)
  {
    this.sources   = sources;
    this.artifacts = artifacts;
    this.type      = type;
    this.name      = name;
  }

  private Semantic.Package check() {
    symbols = AcyclicCache.create(this::checkSymbol);
    var resolution = PackageResolver.resolve(sources, artifacts, name);
    Persistance.record(artifacts, resolution, name, "resolution");
    for (var declaration : resolution.declarations().keys())
      symbols.get(declaration);
    return switch (type) {
      case EXECUTABLE -> new Semantic.Executable(name, symbols.getAll());
      case LIBRARY -> new Semantic.Library(name, symbols.getAll());
      case IMPLEMENTATION ->
        new Semantic.Implementation(name, symbols.getAll());
    };
  }

  private Semantic.Symbol checkSymbol(String name) {
    throw Subject.unimplemented();
  }
}
