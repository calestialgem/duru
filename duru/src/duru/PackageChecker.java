package duru;

import java.nio.file.Path;

public final class PackageChecker {
  public static Semantic.Package check(
    Path sources,
    Path artifacts,
    String name)
  {
    var checker = new PackageChecker(sources, artifacts, name);
    return checker.check();
  }

  private final Path   sources;
  private final Path   artifacts;
  private final String name;

  private PackageChecker(Path sources, Path artifacts, String name) {
    this.sources   = sources;
    this.artifacts = artifacts;
    this.name      = name;
  }

  private Semantic.Package check() {
    var symbols    = AcyclicCache.<String, Semantic.Symbol>create(null);
    var resolution = PackageResolver.resolve(sources, artifacts, name);
    Persistance.record(artifacts, resolution, name, "resolution");
    throw Subject.unimplemented();
  }
}
