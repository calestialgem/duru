package duru;

import java.nio.file.Path;

public final class PackageChecker {
  public static Semantic.Package check(
    CompilerDebugger debugger,
    Object subject,
    SetBuffer<String> externalNames,
    Accessor<Name, Semantic.Package> accessor,
    Path sources,
    PackageType type,
    Name packageName)
  {
    var checker =
      new PackageChecker(
        debugger,
        subject,
        externalNames,
        accessor,
        sources,
        type,
        packageName);
    return checker.check();
  }

  private final CompilerDebugger                 debugger;
  private final Object                           subject;
  private final SetBuffer<String>                externalNames;
  private final Accessor<Name, Semantic.Package> accessor;
  private final Path                             sources;
  private final PackageType                      type;
  private final Name                             packageName;
  private Map<String, Node.Declaration>          declarations;
  private AcyclicCache<String, Semantic.Symbol>  symbols;

  private PackageChecker(
    CompilerDebugger debugger,
    Object subject,
    SetBuffer<String> externalNames,
    Accessor<Name, Semantic.Package> accessor,
    Path sources,
    PackageType type,
    Name packageName)
  {
    this.debugger      = debugger;
    this.subject       = subject;
    this.externalNames = externalNames;
    this.accessor      = accessor;
    this.sources       = sources;
    this.type          = type;
    this.packageName   = packageName;
  }

  private Semantic.Package check() {
    resolveDeclarations();
    symbols = AcyclicCache.create(this::checkSymbol);
    if (packageName.equals(Name.of("duru"))) {
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
      case EXECUTABLE -> new Semantic.Executable(packageName, symbols.getAll());
      case LIBRARY -> new Semantic.Library(packageName, symbols.getAll());
      case IMPLEMENTATION ->
        new Semantic.Implementation(packageName, symbols.getAll());
    };
  }

  private void resolveDeclarations() {
    var directory           = packageName.resolve(sources);
    var packageDeclarations = MapBuffer.<String, Node.Declaration>create();
    for (var file : Persistance.list(subject, directory)) {
      var fullFilename = file.getFileName().toString();
      if (!fullFilename.endsWith(".duru"))
        continue;
      var filename =
        fullFilename.substring(0, fullFilename.length() - ".duru".length());
      var source   = new Source(file, Persistance.load(directory, file));
      debugger.recordSource(source, packageName, filename);
      var tokens = SourceLexer.lex(source);
      debugger.recordTokens(tokens, packageName, filename);
      var declarations = SourceParser.parse(tokens);
      debugger.recordDeclarations(declarations, packageName, filename);
      for (var declaration : declarations) {
        var identifier = declaration.name().text();
        if (packageDeclarations.contains(identifier)) {
          throw Diagnostic
            .error(
              declaration.name().location(),
              "redeclaration of `%s`",
              packageName.scope(identifier));
        }
        packageDeclarations.add(identifier, declaration);
      }
    }
    declarations = packageDeclarations.toMap();
    debugger.recordResolution(declarations, packageName);
  }

  private Semantic.Symbol checkSymbol(Object subject, String identifier) {
    var checked = declarations.get(identifier);
    if (checked.isEmpty()) {
      throw Diagnostic
        .error(
          subject,
          "there is no symbol `%s`",
          packageName.scope(identifier));
    }
    return SymbolChecker
      .check(
        externalNames,
        this::accessSymbol,
        packageName,
        checked.getFirst());
  }

  private Semantic.Symbol accessSymbol(Object subject, Name mention) {
    var mentionedSymbol = mention.getSymbol();
    if (!mention.isScoped()) {
      return symbols.get(subject, mentionedSymbol);
    }
    var mentionedPackage = mention.getPackage();
    if (mentionedPackage.equals(packageName))
      return symbols.get(subject, mentionedSymbol);
    var accessedSymbol =
      accessor.access(subject, mentionedPackage).symbols().get(mentionedSymbol);
    if (accessedSymbol.isEmpty()) {
      throw Diagnostic.error(subject, "there is no symbol `%s`", mention);
    }
    if (!accessedSymbol.getFirst().isPublic()) {
      throw Diagnostic
        .error(subject, "accessed symbol `%s` is not public", mention);
    }
    return accessedSymbol.getFirst();
  }
}
