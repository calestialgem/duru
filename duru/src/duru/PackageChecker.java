package duru;

import java.nio.file.Path;

public final class PackageChecker {
  public static Semantic.Package check(
    Accessor<String, Semantic.Package> accessor,
    Path sources,
    Path artifacts,
    PackageType type,
    String name)
  {
    var checker = new PackageChecker(accessor, sources, artifacts, type, name);
    return checker.check();
  }

  private final Accessor<String, Semantic.Package> accessor;
  private final Path                               sources;
  private final Path                               artifacts;
  private final PackageType                        type;
  private final String                             name;
  private Map<String, Node.Declaration>            declarations;
  private AcyclicCache<String, Semantic.Symbol>    symbols;

  private PackageChecker(
    Accessor<String, Semantic.Package> accessor,
    Path sources,
    Path artifacts,
    PackageType type,
    String name)
  {
    this.accessor  = accessor;
    this.sources   = sources;
    this.artifacts = artifacts;
    this.type      = type;
    this.name      = name;
  }

  private Semantic.Package check() {
    resolveDeclarations();
    symbols = AcyclicCache.create(this::checkSymbol);
    for (var declaration : declarations.keys())
      symbols.get(declaration);
    return switch (type) {
      case EXECUTABLE -> new Semantic.Executable(name, symbols.getAll());
      case LIBRARY -> new Semantic.Library(name, symbols.getAll());
      case IMPLEMENTATION ->
        new Semantic.Implementation(name, symbols.getAll());
    };
  }

  private void resolveDeclarations() {
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
    declarations = packageDeclarations.toMap();
    Persistance.record(artifacts, declarations, name, "declarations");
  }

  private Semantic.Symbol checkSymbol(String symbolName) {
    return SymbolChecker
      .check(this::accessSymbol, name, declarations.get(symbolName).getFirst());
  }

  private Semantic.Symbol accessSymbol(String accessedSymbol) {
    var symbolName = Text.getSymbol(accessedSymbol);
    if (accessedSymbol.equals(symbolName))
      return symbols.get(symbolName);
    var accessedPackage = Text.getPackage(accessedSymbol);
    if (accessedPackage.equals(name)) {
      return symbols.get(symbolName);
    }
    var accessed = accessor.access(accessedPackage).symbols().get(symbolName);
    if (accessed.isEmpty())
      throw Subject
        .error(
          "there is no symbol `%s` in package `%s`",
          symbolName,
          accessedPackage);
    if (!accessed.getFirst().isPublic())
      throw Subject.error("accessed symbol `%s` is not public", accessedSymbol);
    return accessed.getFirst();
  }
}
