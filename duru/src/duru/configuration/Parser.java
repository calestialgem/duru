package duru.configuration;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import duru.Namespace;
import duru.diagnostic.Subject;

final class Parser {
  static Configuration parse(Path file, String contents, List<Token> tokens) {
    var parser = new Parser(file, contents, tokens);
    return parser.parse();
  }

  private final Path file;

  private final String contents;

  private final List<Token> tokens;

  private String name;

  private List<Reference> executables;

  private int index;

  private Parser(Path file, String contents, List<Token> tokens) {
    this.file     = file;
    this.contents = contents;
    this.tokens   = tokens;
  }

  private Configuration parse() {
    executables = new ArrayList<>();
    index       = 0;
    expectToken(Token.Project.class, "project definition");
    name =
      expectToken(Token.Identifier.class, "name of the project definition")
        .word();
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
    return new Configuration(name, executables);
  }

  private Optional<Reference> parsePackageName() {
    var start = index;
    var name  = parseToken(Token.Identifier.class);
    if (name.isEmpty()) {
      return Optional.empty();
    }
    var buffer = new StringBuilder();
    buffer.append(name.get().word());
    while (parseToken(Token.Dot.class).isPresent()) {
      buffer.append('.');
      buffer
        .append(
          expectToken(Token.Identifier.class, "name of the package").word());
    }
    return Optional
      .of(
        new Reference(
          Subject.of(file, contents, start, index),
          new Namespace(buffer.toString())));
  }

  private <T extends Token> T expectToken(Class<T> klass, String explanation) {
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

  private <T> T expect(ParseFunction<T> parseFunction, String explanation) {
    var result = parseFunction.parse();
    if (result.isPresent()) {
      return result.get();
    }
    if (index == tokens.size()) {
      if (index != 0) {
        var previous = tokens.getLast();
        throw Subject
          .of(file, contents, previous.start(), previous.end())
          .diagnose(
            "error",
            "Expected %s at the end of the file, after %s!",
            explanation,
            previous.explain())
          .toException();
      }
      throw Subject
        .of(file)
        .diagnose("error", "Expected %s instead of an empty file!", explanation)
        .toException();
    }
    var current = tokens.get(index);
    throw Subject
      .of(file, contents, current.start(), current.end())
      .diagnose(
        "error",
        "Expected %s instead of %s!",
        explanation,
        current.explain())
      .toException();
  }
}
