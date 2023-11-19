package duru;

import java.nio.file.Path;

public final class PackageChecker {
  public static Semantic.Package check(
    CompilerDebugger debugger,
    Object subject,
    Accessor<String, Semantic.Package> accessor,
    Path sources,
    Path artifacts,
    PackageType type,
    String name)
  {
    var checker =
      new PackageChecker(
        debugger,
        subject,
        accessor,
        sources,
        artifacts,
        type,
        name);
    return checker.check();
  }

  private final CompilerDebugger                   debugger;
  private final Object                             subject;
  private final Accessor<String, Semantic.Package> accessor;
  private final Path                               sources;
  private final Path                               artifacts;
  private final PackageType                        type;
  private final String                             name;
  private Map<String, Node.Declaration>            declarations;
  private AcyclicCache<String, Semantic.Symbol>    symbols;

  private PackageChecker(
    CompilerDebugger debugger,
    Object subject,
    Accessor<String, Semantic.Package> accessor,
    Path sources,
    Path artifacts,
    PackageType type,
    String name)
  {
    this.debugger  = debugger;
    this.subject   = subject;
    this.accessor  = accessor;
    this.sources   = sources;
    this.artifacts = artifacts;
    this.type      = type;
    this.name      = name;
  }

  private Semantic.Package check() {
    resolveDeclarations();
    symbols = AcyclicCache.create(this::checkSymbol);
    if (name.equals("duru")) {
      var builtins =
        List.<Semantic
          .Builtin>of(
            new Semantic.Byte(),
            new Semantic.Boolean(),
            new Semantic.Natural32(),
            new Semantic.Integer32(),
            new Semantic.Unit(),
            new Semantic.Noreturn());
      for (var builtin : builtins) {
        symbols.add(builtin.identifier(), builtin);
      }
    }
    for (var declaration : declarations) {
      symbols.get(declaration.value().location(), declaration.key());
    }
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
    for (var file : Persistance.list(subject, directory)) {
      var fullFilename = file.getFileName().toString();
      var filename     =
        fullFilename.substring(0, fullFilename.length() - ".duru".length());
      var source       = new Source(file, Persistance.load(directory, file));
      debugger.recordSource(artifacts, source, name, filename);
      var tokens = SourceLexer.lex(source);
      debugger.recordTokens(artifacts, tokens, name, filename);
      var declarations = SourceParser.parse(tokens);
      debugger.recordDeclarations(artifacts, declarations, name, filename);
      for (var declaration : declarations) {
        var identifier = declaration.name().text();
        if (packageDeclarations.contains(identifier)) {
          throw Diagnostic
            .error(
              declaration.name().location(),
              "redeclaration of `%s.%s`",
              name,
              identifier);
        }
        packageDeclarations.add(identifier, declaration);
      }
    }
    declarations = packageDeclarations.toMap();
    debugger.recordResolution(artifacts, declarations, name);
  }

  private Semantic.Symbol checkSymbol(Object subject, String symbolName) {
    var checked = declarations.get(symbolName);
    if (checked.isEmpty()) {
      throw Diagnostic
        .error(
          subject,
          "there is no symbol `%s` in package `%s`",
          Text.getSymbol(symbolName),
          name);
    }
    return SymbolChecker.check(this::accessSymbol, name, checked.getFirst());
  }

  private Semantic.Symbol accessSymbol(Object subject, String accessedSymbol) {
    var symbolName = Text.getSymbol(accessedSymbol);
    if (accessedSymbol.equals(symbolName)) {
      return symbols.get(subject, symbolName);
    }
    var accessedPackage = Text.getPackage(accessedSymbol);
    if (accessedPackage.equals(name)) {
      return symbols.get(subject, symbolName);
    }
    var accessed =
      accessor.access(subject, accessedPackage).symbols().get(symbolName);
    if (accessed.isEmpty()) {
      throw Diagnostic
        .error(
          subject,
          "there is no symbol `%s` in package `%s`",
          symbolName,
          accessedPackage);
    }
    if (!accessed.getFirst().isPublic()) {
      throw Diagnostic
        .error(subject, "accessed symbol `%s` is not public", accessedSymbol);
    }
    return accessed.getFirst();
  }
}
