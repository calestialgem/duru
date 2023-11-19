package duru;

import java.nio.file.Path;

public final class PackageCompiler {
  public static Semantic.Package compile(
    Path sources,
    Path artifacts,
    PackageType type,
    String name)
  {
    var checker = new PackageCompiler(sources, artifacts, type, name);
    return checker.compile();
  }

  private final Path                            sources;
  private final Path                            artifacts;
  private final PackageType                     type;
  private final String                          name;
  private AcyclicCache<String, Semantic.Symbol> symbols;
  private Resolution                            resolution;

  private PackageCompiler(
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

  private Semantic.Package compile() {
    symbols    = AcyclicCache.create(this::compileSymbol);
    resolution = PackageResolver.resolve(sources, artifacts, name);
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

  private Semantic.Symbol compileSymbol(String symbolName) {
    return SymbolChecker
      .check(
        this::accessSymbol,
        name,
        resolution.declarations().get(symbolName).getFirst());
  }

  private Semantic.Symbol accessSymbol(String symbolName) {
    throw Subject.unimplemented();
  }
}
