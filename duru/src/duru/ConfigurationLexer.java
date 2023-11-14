package duru;

public final class ConfigurationLexer {
  public static Lectics lex(String text) {
    var lexer = new ConfigurationLexer(text);
    return lexer.lex();
  }

  private final String         text;
  private ListBuffer<Tag>      tokens;
  private ListBuffer<Location> locations;
  private int                  index;
  private int                  begin;

  private ConfigurationLexer(String text) {
    this.text = text;
  }

  private Lectics lex() {
    tokens    = ListBuffer.create();
    locations = ListBuffer.create();
    index     = 0;
    while (hasCharacter()) {
      begin = index;
      var initial = getCharacter();
      advance();
      switch (initial) {
        case ' ', '\n' -> {}
        case '\r' -> {
          if (!hasCharacter() || getCharacter() != '\n')
            throw Diagnostic.error("incomplete CRLF sequence at %d", begin);
          advance();
        }
        case '/' -> {
          if (hasCharacter() && getCharacter() == '/') {
            advance();
            while (hasCharacter() && getCharacter() != '\n')
              advance();
            break;
          }
          if (!hasCharacter() || getCharacter() != '*')
            throw Diagnostic.error("incomplete comment at %d", begin);
          advance();
          var blockComments = 1;
          while (hasCharacter()) {
            var character = getCharacter();
            advance();
            if (character == '/' && hasCharacter() && getCharacter() == '*') {
              advance();
              blockComments++;
              continue;
            }
            if (character == '*' && hasCharacter() && getCharacter() == '/') {
              advance();
              blockComments--;
              if (blockComments == 0)
                break;
              continue;
            }
          }
          if (blockComments != 0)
            throw Diagnostic.error("incomplete block comment at %d", begin);
        }
        case '{' -> tokenize(Tag.openingBrace);
        case '}' -> tokenize(Tag.closingBrace);
        case ';' -> tokenize(Tag.semicolon);
        default -> {
          if (Character.isLetter(initial)) {
            while (hasCharacter() && Character.isLetterOrDigit(getCharacter()))
              advance();
            var word = text.substring(begin, index);
            switch (word) {
              case "project" -> tokenize(Tag.project);
              case "executable" -> tokenize(Tag.executable);
              default -> tokenize(Tag.identifier);
            }
            break;
          }
          throw Diagnostic
            .error(
              "illegal codepoint `%c` (%06x) at %d",
              initial,
              initial,
              begin);
        }
      }
    }
    return new Lectics(text, tokens.toList(), locations.toList());
  }

  private void tokenize(Tag type) {
    tokens.add(type);
    locations.add(new Location(begin, index));
  }

  private int advance() {
    return index = text.offsetByCodePoints(index, 1);
  }

  private int getCharacter() {
    return text.codePointAt(index);
  }

  private boolean hasCharacter() {
    return index != text.length();
  }
}
