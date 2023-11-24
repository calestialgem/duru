package duru;

public final class ConfigurationLexer {
  public static List<ConfigurationToken> lex(Source source) {
    var lexer = new ConfigurationLexer(source);
    return lexer.lex();
  }

  private final Source                   source;
  private ListBuffer<ConfigurationToken> tokens;
  private int                            index;
  private int                            begin;
  private int                            initial;

  private ConfigurationLexer(Source source) {
    this.source = source;
  }

  private List<ConfigurationToken> lex() {
    tokens = ListBuffer.create();
    index  = 0;
    while (hasCharacter()) {
      begin   = index;
      initial = getCharacter();
      advance();
      switch (initial) {
        case ' ', '\r', '\n' -> {}
        case '/' -> {
          if (hasCharacter() && getCharacter() == '/') {
            advance();
            while (hasCharacter() && getCharacter() != '\n') {
              advance();
            }
            break;
          }
          if (!hasCharacter() || getCharacter() != '*') {
            throw Diagnostic.error(location(), "incomplete comment");
          }
          advance();
          var blockComments = 1;
          while (hasCharacter()) {
            var character = getCharacter();
            advance();
            if (character == '*' && hasCharacter() && getCharacter() == '/') {
              advance();
              blockComments--;
              if (blockComments == 0) {
                break;
              }
            }
            if (character == '/' && hasCharacter() && getCharacter() == '*') {
              advance();
              blockComments++;
            }
          }
          if (blockComments != 0) {
            throw Diagnostic.error(location(), "incomplete block comment");
          }
        }
        case ';' -> tokens.add(new ConfigurationToken.Semicolon(location()));
        case ':' -> {
          if (!hasCharacter() && getCharacter() != ':') {
            throw Diagnostic.error(location(), "incomplete scope token");
          }
          advance();
          tokens.add(new ConfigurationToken.ColonColon(location()));
        }
        default -> {
          if (Text.isIdentifierInitial(initial)) {
            while (hasCharacter() && Text.isIdentifierBody(getCharacter())) {
              advance();
            }
            var text = source.contents().substring(begin, index);
            switch (text) {
              case "executable" ->
                tokens.add(new ConfigurationToken.Executable(location()));
              case "library" ->
                tokens.add(new ConfigurationToken.Library(location()));
              default ->
                tokens.add(new ConfigurationToken.Identifier(location(), text));
            }
            break;
          }
          throw Diagnostic.error(location(), "unknown character `%c`", initial);
        }
      }
    }
    return tokens.toList();
  }

  private Location location() {
    return location(begin);
  }

  private Location location(int begin) {
    return Location.at(source, begin, index);
  }

  private boolean hasCharacter() {
    return index != source.contents().length();
  }

  private int getCharacter() {
    return source.contents().codePointAt(index);
  }

  private void advance() {
    index = source.contents().offsetByCodePoints(index, 1);
  }
}
