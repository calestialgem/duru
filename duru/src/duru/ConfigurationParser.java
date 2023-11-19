package duru;

import java.util.function.Supplier;

public final class ConfigurationParser {
  public static List<ConfigurationNode.PackageDeclaration> parse(
    List<ConfigurationToken> tokens)
  {
    var parser = new ConfigurationParser(tokens);
    return parser.parse();
  }

  private final List<ConfigurationToken> tokens;
  private int                            index;

  private ConfigurationParser(List<ConfigurationToken> tokens) {
    this.tokens = tokens;
  }

  private List<ConfigurationNode.PackageDeclaration> parse() {
    index = 0;
    var declarations =
      ListBuffer.<ConfigurationNode.PackageDeclaration>create();
    while (index != tokens.length()) {
      var declaration = expect(this::parsePackageDeclaration, "declaration");
      declarations.add(declaration);
    }
    return declarations.toList();
  }

  private Optional<ConfigurationNode.PackageDeclaration> parsePackageDeclaration() {
    return or(this::parseExecutable, this::parseLibrary);
  }

  private Optional<ConfigurationNode.Executable> parseExecutable() {
    var begin = index;
    if (parse(ConfigurationToken.Executable.class).isEmpty()) {
      return Optional.absent();
    }
    var name = expect(this::parsePackageName, "executable name");
    expect(ConfigurationToken.Semicolon.class, "`;` of executable declaration");
    return Optional
      .present(new ConfigurationNode.Executable(location(begin), name));
  }

  private Optional<ConfigurationNode.Library> parseLibrary() {
    var begin = index;
    if (parse(ConfigurationToken.Library.class).isEmpty()) {
      return Optional.absent();
    }
    var name = expect(this::parsePackageName, "library name");
    expect(ConfigurationToken.Semicolon.class, "`;` of library declaration");
    return Optional
      .present(new ConfigurationNode.Library(location(begin), name));
  }

  private Optional<ConfigurationNode.PackageName> parsePackageName() {
    var begin = index;
    var name  = parse(ConfigurationToken.Identifier.class);
    if (name.isEmpty()) {
      return Optional.absent();
    }
    var subspaces = ListBuffer.<ConfigurationToken.Identifier>create();
    subspaces.add(name.getLast());
    while (!parse(ConfigurationToken.Dot.class).isEmpty()) {
      var subspace = expect(ConfigurationToken.Identifier.class, "name");
      subspaces.add(subspace);
    }
    return Optional
      .present(
        new ConfigurationNode.PackageName(location(begin), subspaces.toList()));
  }

  private Location location(int begin) {
    var beginLocation = tokens.get(begin).location();
    var endLocation   = tokens.get(index - 1).location();
    return new Location(
      beginLocation.source(),
      beginLocation.begin(),
      endLocation.end());
  }

  private <Token extends ConfigurationToken> Token expect(
    Class<Token> tokenClass,
    String explanation)
  {
    return expect(() -> parse(tokenClass), explanation);
  }

  private <Value> Value expect(
    Supplier<Optional<Value>> parserFunction,
    String explanation)
  {
    var value = parserFunction.get();
    if (!value.isEmpty()) {
      return value.getLast();
    }
    throw missing(explanation);
  }

  private RuntimeException missing(String explanation) {
    if (index == 0) {
      return Diagnostic
        .error(
          tokens.getFirst().location(),
          "expected %s instead of %s at beginning of file",
          explanation,
          tokens.getFirst());
    }
    if (index == tokens.length()) {
      return Diagnostic
        .error(
          tokens.getLast().location(),
          "expected %s after %s at end of file",
          explanation,
          tokens.getLast());
    }
    return Diagnostic
      .error(
        tokens.get(index).location(),
        "expected %s instead of %s after %s",
        explanation,
        tokens.getLast(),
        tokens.get(index - 1));
  }

  @SuppressWarnings("unchecked")
  private <Token extends ConfigurationToken> Optional<Token> parse(
    Class<Token> tokenClass)
  {
    if (index == tokens.length()) {
      return Optional.absent();
    }
    var token = tokens.get(index);
    if (!tokenClass.isInstance(token)) {
      return Optional.absent();
    }
    index++;
    return Optional.present((Token) token);
  }

  @SafeVarargs
  @SuppressWarnings("unchecked")
  private <Value> Optional<Value> or(
    Supplier<Optional<? extends Value>>... parserFunctions)
  {
    for (var parserFunction : parserFunctions) {
      var value = parserFunction.get();
      if (!value.isEmpty()) {
        return (Optional<Value>) value;
      }
    }
    return Optional.absent();
  }
}
