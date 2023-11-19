package duru;

import java.nio.file.Path;

public final class PackageResolver {
  public static Resolution resolve(Path sources, Path artifacts, String name) {
    var resolver = new PackageResolver(sources, artifacts, name);
    return resolver.resolve();
  }

  private final Path   sources;
  private final Path   artifacts;
  private final String name;

  private PackageResolver(Path sources, Path artifacts, String name) {
    this.sources   = sources;
    this.artifacts = artifacts;
    this.name      = name;
  }

  private Resolution resolve() {
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
    var packageDeclarations = MapBuffer.<String, Node.Declaration>create();
    for (var file : Persistance.list(directory)) {
      var fullFilename = file.getFileName().toString();
      var filename     =
        fullFilename.substring(0, fullFilename.length() - ".duru".length());
      var source       = new Source(file, Persistance.load(file));
      Persistance.record(artifacts, source, name, filename, "source");
      var tokens = SourceLexer.lex(source);
      Persistance.record(artifacts, tokens, name, filename, "tokens");
      var declarations = SourceParser.parse(tokens);
      Persistance
        .record(artifacts, declarations, name, filename, "declarations");
      for (var declaration : declarations) {
        var identifier = declaration.name().text();
        if (packageDeclarations.contains(identifier)) {
          throw Subject.error("redeclaration of `%s`", identifier);
        }
        packageDeclarations.add(identifier, declaration);
      }
    }
    return new Resolution(name, packageDeclarations.toMap());
  }
}
