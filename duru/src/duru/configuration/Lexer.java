package duru.configuration;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import duru.diagnostic.Subject;

final class Lexer {
  static List<Token> lex(Path file, String contents) {
    var lexer = new Lexer(file, contents);
    return lexer.lex();
  }

  private final Path file;

  private final String contents;

  private List<Token> tokens;

  private int index;

  private Lexer(Path file, String contents) {
    this.file     = file;
    this.contents = contents;
  }

  private List<Token> lex() {
    tokens = new ArrayList<>();
    index  = 0;
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
              default -> new Token.Identifier(start, word);
            });
            break;
          }
          throw Subject
            .of(file, contents, start, index)
            .diagnose("error", "Unknown character `%c`!", initial)
            .toException();
        }
      }
    }
    return tokens;
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
