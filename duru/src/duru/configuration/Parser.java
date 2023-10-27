package duru.configuration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

final class Parser {
  private String contents;

  private List<Token> tokens;

  private String name;

  private final List<PackageName> executables;

  private int index;

  private Parser(
    String contents,
    List<Token> tokens,
    String name,
    List<PackageName> executables,
    int index)
  {
    this.contents    = contents;
    this.tokens      = tokens;
    this.name        = name;
    this.executables = executables;
    this.index       = index;
  }

  static Parser create() {
    return new Parser(null, null, null, new ArrayList<>(), 0);
  }

  Configuration parse(String contents, List<Token> tokens)
    throws ConfigurationParseException
  {
    this.contents = contents;
    this.tokens   = List.copyOf(tokens);
    executables.clear();
    index = 0;
    parse();
    return new Configuration(name, executables);
  }

  private void parse() throws ConfigurationParseException {
    expectToken(Token.Project.class, "project definition");
    name =
      expectToken(Token.Identifier.class, "name of the project definition")
        .text(contents);
    expectToken(
      Token.OpeningBrace.class,
      "opening `{` of the project definition");
    while (true) {
      if (parseToken(Token.Executable.class).isPresent()) {
        executables
          .add(
            expect(
              this::parsePackageName,
              "package of the executable directive"));
        expectToken(
          Token.Semicolon.class,
          "terminating `;` of the executable directive");
      }
      expectToken(
        Token.ClosingBrace.class,
        "closing `}` of the project definition");
      break;
    }
  }

  private Optional<PackageName> parsePackageName()
    throws ConfigurationParseException
  {
    var name = parseToken(Token.Identifier.class);
    if (name.isEmpty()) {
      return Optional.empty();
    }
    var scopes = new ArrayList<String>();
    scopes.add(name.get().text(contents));
    while (parseToken(Token.Dot.class).isPresent()) {
      scopes
        .add(
          expectToken(Token.Identifier.class, "name of the package")
            .text(contents));
    }
    return Optional.of(new PackageName(scopes));
  }

  private <T extends Token> T expectToken(Class<T> klass, String explanation)
    throws ConfigurationParseException
  {
    return expect(() -> parseToken(klass), explanation);
  }

  @SuppressWarnings("unchecked")
  private <T extends Token> Optional<T> parseToken(Class<T> klass) {
    if (index == tokens.size()) {
      return Optional.empty();
    }
    var token = tokens.get(index);
    if (!klass.isInstance(token)) {
      return Optional.empty();
    }
    index++;
    return Optional.of((T) token);
  }

  private <T> T expect(ParseFunction<T> parseFunction, String explanation)
    throws ConfigurationParseException
  {
    var result = parseFunction.parse();
    if (result.isPresent()) {
      return result.get();
    }
    if (index == tokens.size()) {
      if (index != 0) {
        var previous = tokens.getLast();
        throw ConfigurationParseException
          .create(
            contents,
            previous.start(),
            previous.length(),
            "Expected %s at the end of the file, after %s!",
            explanation,
            previous.explain(contents));
      }
      throw ConfigurationParseException
        .create(
          contents,
          0,
          0,
          "Expected %s instead of an empty file!",
          explanation);
    }
    var current = tokens.get(index);
    throw ConfigurationParseException
      .create(
        contents,
        current.start(),
        current.length(),
        "Expected %s instead of %s!",
        explanation,
        current.explain(contents));
  }
}
