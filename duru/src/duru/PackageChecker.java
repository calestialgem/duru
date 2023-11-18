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
    var directory = sources;
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
    var symbols = AcyclicCache.<String, Semantic.Symbol>create(null);
    for (var file : Persistance.list(directory)) {
      var source       = new Source(file, Persistance.load(file));
      var fullFilename = file.getFileName().toString();
      var filename     =
        fullFilename.substring(0, fullFilename.length() - ".duru".length());
      Persistance.record(artifacts, source, name, filename, "source");
    }
    throw Subject.unimplemented();
  }
}
