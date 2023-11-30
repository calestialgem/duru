package duru;

import java.nio.file.Path;
import java.util.Formatter;

public final class Lexer {
  public static Lexer create() {
    return new Lexer(Lectics_Buffer.create(), null, null, 0, 0, '\0', 0, 0);
  }

  private final Lectics_Buffer lectics;
  private Path path;
  private String contents;
  private int index;
  private int begin;
  private char initial;
  private int line;
  private int column;

  private Lexer(
    Lectics_Buffer lectics,
    Path path,
    String contents,
    int index,
    int begin,
    char initial,
    int line,
    int column)
  {
    this.lectics = lectics;
    this.path = path;
    this.contents = contents;
    this.index = index;
    this.begin = begin;
    this.initial = initial;
    this.line = line;
    this.column = column;
  }

  public Lectics lex(Path path, String contents) {
    lectics.clear();
    this.path = path;
    this.contents = contents;
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
        case '{' -> lectics.add_token(Lectics.OPENING_BRACE, begin);
        case '}' -> lectics.add_token(Lectics.CLOSING_BRACE, begin);
        default -> {
          if (Text.is_identifier_initial(initial)) {
            while (has_character()
              && Text.is_identifier_body(get_character()))
            {
              advance();
            }
            var text = contents.substring(begin, index);
            switch (text) {
              case "entrypoint" ->
                lectics.add_token(Lectics.KEYWORD_ENTRYPOINT, begin);
              default -> throw Diagnostic.unimplemented("");
            }
            break;
          }
          throw error("unknown character `%c`", initial);
        }
      }
    }
    return lectics.bake(path, contents);
  }

  private RuntimeException error(String format, Object... arguments) {
    var subject = new StringBuilder();
    try (var f = new Formatter(subject)) {
      f.format("%s:%d.%d-%d", path, line, column - 1, column);
    }
    return Diagnostic.error(subject, format, arguments);
  }

  private boolean has_character() {
    return index != contents.length();
  }

  private char get_character() {
    return contents.charAt(index);
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
}
