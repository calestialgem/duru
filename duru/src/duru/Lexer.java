package duru;

import java.nio.file.Path;

public final class Lexer {
  private final Lectics_Buffer lectics;
  private Path path;
  private String content;
  private int index;
  private int begin;
  private char initial;
  private int line;
  private int column;

  public Lexer() {
    lectics = new Lectics_Buffer();
  }

  public Lectics lex(Path path, String content) {
    lectics.clear();
    this.path = path;
    this.content = content;
    index = 0;
    line = 1;
    column = 1;
    while (has_character()) {
      begin = index;
      initial = get_character();
      advance();
      switch (initial) {
        case ' ', '\r', '\n' -> {}
        case '/' -> {
          if (!has_character()) {
            throw error("incomplete comment");
          }
          if (get_character() == '/') {
            advance();
            while (has_character() && get_character() != '\n') {
              advance();
            }
            break;
          }
          if (get_character() != '*') {
            throw error("incomplete comment");
          }
          advance();
          var blockComments = 1;
          while (has_character()) {
            var character = get_character();
            advance();
            if (character == '*' && has_character() && get_character() == '/') {
              advance();
              blockComments--;
              if (blockComments == 0) {
                break;
              }
            }
            if (character == '/' && has_character() && get_character() == '*') {
              advance();
              blockComments++;
            }
          }
          if (blockComments != 0) {
            throw error("incomplete block comment");
          }
        }
        case '{' -> add_token(Token.OPENING_BRACE);
        case '}' -> add_token(Token.CLOSING_BRACE);
        default -> {
          if (Text.is_identifier_initial(initial)) {
            while (has_character()
              && Text.is_identifier_body(get_character()))
            {
              advance();
            }
            var text = content.substring(begin, index);
            switch (text) {
              case "entrypoint" -> add_token(Token.ENTRYPOINT);
              default -> add_varying_token(Token.IDENTIFIER);
            }
            break;
          }
          throw error("unknown character `%c`", initial);
        }
      }
    }
    return lectics.bake(path, content);
  }

  private RuntimeException error(String format, Object... arguments) {
    return Diagnostic.error(subject(), format, arguments);
  }

  private String subject() {
    return "%s:%d.%d-%d".formatted(path, line, column - 1, column);
  }

  private boolean has_character() {
    return index != content.length();
  }

  private char get_character() {
    return content.charAt(index);
  }

  private void advance() {
    if (get_character() == '\n') {
      line++;
      column = 1;
    }
    else {
      column++;
    }
    index++;
  }

  private void add_token(Token kind) {
    lectics.add(kind, begin);
  }

  private void add_varying_token(Token kind) {
    lectics.add_varying(kind, begin, index);
  }
}
