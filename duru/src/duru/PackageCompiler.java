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
  private Map<String, Node.Declaration>         declarations;
  private Map<String, Semantic.Type>            types;
  private AcyclicCache<String, Semantic.Symbol> symbols;

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
    resolveDeclarations();
    checkSignatures();
    symbols = AcyclicCache.create(this::compileSymbol);
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

  private void checkSignatures() {
    types = declarations.transformValues(this::checkSignature);
    Persistance.record(artifacts, types, name, "types");
  }

  private Semantic.Type checkSignature(Node.Declaration declaration) {
    return switch (declaration) {
      case Node.Proc proc -> throw Subject.unimplemented();
      case Node.ExternalProc proc -> throw Subject.unimplemented();
      case Node.Struct struct ->
        new Semantic.Struct(name, struct.isPublic(), struct.name().text());
    };
  }

  private Semantic.Symbol compileSymbol(String symbolName) {
    return SymbolChecker
      .check(this::accessSymbol, name, declarations.get(symbolName).getFirst());
  }

  private Semantic.Symbol accessSymbol(String symbolName) {
    throw Subject.unimplemented();
  }
}
