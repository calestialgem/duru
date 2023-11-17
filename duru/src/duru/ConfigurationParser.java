package duru;

import java.util.function.Supplier;

public final class ConfigurationParser {
  public static ConfigurationNode.Module parse(
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

  private ConfigurationNode.Module parse() {
    index = 0;
    expect(
      ConfigurationToken.Module.class,
      "keyword of the module declaration");
    var name =
      expect(
        ConfigurationToken.Identifier.class,
        "name of the module declaration");
    expect(
      ConfigurationToken.OpeningBrace.class,
      "package list opener `{` of the module declaration");
    var declarations =
      ListBuffer.<ConfigurationNode.PackageDeclaration>create();
    while (parse(ConfigurationToken.ClosingBrace.class).isEmpty()) {
      var declaration =
        expect(
          this::parsePackageDeclaration,
          "package declaration or the package list closer `}` of the module declaration");
      declarations.add(declaration);
    }
    if (index != tokens.length()) {
      throw Subject.error("expected the end of the file");
    }
    return new ConfigurationNode.Module(
      location(0),
      name,
      declarations.toList());
  }

  private Optional<ConfigurationNode.PackageDeclaration> parsePackageDeclaration() {
    return or(this::parseExecutable, this::parseLibrary);
  }

  private Optional<ConfigurationNode.Executable> parseExecutable() {
    var begin = index;
    if (parse(ConfigurationToken.Executable.class).isEmpty())
      return Optional.absent();
    var name =
      expect(
        this::parsePackageName,
        "name of the executable package declaration");
    expect(
      ConfigurationToken.Semicolon.class,
      "terminator of the executable package declaration");
    return Optional
      .present(new ConfigurationNode.Executable(location(begin), name));
  }

  private Optional<ConfigurationNode.Library> parseLibrary() {
    var begin = index;
    if (parse(ConfigurationToken.Library.class).isEmpty())
      return Optional.absent();
    var name =
      expect(this::parsePackageName, "name of the library package declaration");
    expect(
      ConfigurationToken.Semicolon.class,
      "terminator of the library package declaration");
    return Optional
      .present(new ConfigurationNode.Library(location(begin), name));
  }

  private Optional<ConfigurationNode.PackageName> parsePackageName() {
    var begin = index;
    var name  = parse(ConfigurationToken.Identifier.class);
    if (name.isEmpty())
      return Optional.absent();
    var subspaces = ListBuffer.<ConfigurationToken.Identifier>create();
    subspaces.add(name.getLast());
    while (!parse(ConfigurationToken.Dot.class).isEmpty()) {
      var subspace =
        expect(
          ConfigurationToken.Identifier.class,
          "subspace of the package name");
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
    if (!value.isEmpty())
      return value.getLast();
    throw Subject.error("expected %s", explanation);
  }

  @SuppressWarnings("unchecked")
  private <Token extends ConfigurationToken> Optional<Token> parse(
    Class<Token> tokenClass)
  {
    if (index == tokens.length())
      return Optional.absent();
    var token = tokens.get(index);
    if (!tokenClass.isInstance(token))
      return Optional.absent();
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
      if (!value.isEmpty())
        return (Optional<Value>) value;
    }
    return Optional.absent();
  }
}
