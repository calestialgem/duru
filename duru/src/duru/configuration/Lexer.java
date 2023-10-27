package duru.configuration;

import java.util.ArrayList;
import java.util.List;

final class Lexer {
  private String contents;

  private final List<Token> tokens;

  private int index;

  private Lexer(String contents, List<Token> tokens, int index) {
    this.contents = contents;
    this.tokens   = tokens;
    this.index    = index;
  }

  static Lexer create() {
    return new Lexer(null, new ArrayList<>(), 0);
  }

  List<Token> lex(String contents) throws ConfigurationParseException {
    this.contents = contents;
    tokens.clear();
    index = 0;
    lex();
    return List.copyOf(tokens);
  }

  private void lex() throws ConfigurationParseException {
    while (hasCharacter()) {
      var start   = index;
      var initial = getCharacter();
      advance();
      switch (initial) {
        case ' ', '\t', '\n' -> {}
        case '\r' -> {
          if (hasCharacter() && getCharacter() == '\n') {
            advance();
          }
        }
        case '#' -> {
          while (hasCharacter() && getCharacter() != '\n') {
            advance();
          }
          advance();
        }
        case '{' -> tokens.add(new Token.OpeningBrace(start));
        case '}' -> tokens.add(new Token.ClosingBrace(start));
        case ';' -> tokens.add(new Token.Semicolon(start));
        case '.' -> tokens.add(new Token.Dot(start));
        default -> {
          if (isLetter(initial)) {
            while (hasCharacter() && isWord(getCharacter())) {
              advance();
            }
            var word = getText(start);
            tokens.add(switch (word) {
              case "project" -> new Token.Project(start);
              case "executable" -> new Token.Executable(start);
              default -> new Token.Identifier(start, word.length());
            });
            break;
          }
          throw ConfigurationParseException
            .create(contents, start, 1, "Unknown character `%c`!", initial);
        }
      }
    }
  }

  private boolean hasCharacter() {
    return index != contents.length();
  }

  private int getCharacter() {
    return contents.codePointAt(index);
  }

  private void advance() {
    index = contents.offsetByCodePoints(index, 1);
  }

  private boolean isWord(int character) {
    return isLetter(character) || character >= '0' && character <= '9';
  }

  private boolean isLetter(int character) {
    return character >= 'a' && character <= 'z'
      || character >= 'A' && character <= 'Z';
  }

  private String getText(int start) {
    return contents.substring(start, index);
  }
}
