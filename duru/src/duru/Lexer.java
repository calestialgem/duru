package duru;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Formatter;

public final class Lexer {
  private byte[] token_types;
  private int[] token_begins;
  private int token_count;
  private Path path;
  private String contents;
  private int index;
  private int begin;
  private char initial;
  private int line;
  private int column;

  public Lexer() {
    token_types = new byte[0];
    token_begins = new int[0];
  }

  public Lectics lex(Path path, String contents) {
    token_count = 0;
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
        case '{' -> add_token(Lectics.OPENING_BRACE);
        case '}' -> add_token(Lectics.CLOSING_BRACE);
        default -> {
          if (Text.is_identifier_initial(initial)) {
            while (has_character()
              && Text.is_identifier_body(get_character()))
            {
              advance();
            }
            var text = contents.substring(begin, index);
            switch (text) {
              case "entrypoint" -> add_token(Lectics.KEYWORD_ENTRYPOINT);
              default -> throw Diagnostic.unimplemented("");
            }
            break;
          }
          throw error("unknown character `%c`", initial);
        }
      }
    }
    return new Lectics(path, contents, token_types, token_begins, token_count);
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

  private void add_token(byte token_type) {
    if (token_count == token_types.length) {
      var new_capacity = token_count * 2;
      if (new_capacity == 0)
        new_capacity = 1;
      token_types = Arrays.copyOf(token_types, new_capacity);
      token_begins = Arrays.copyOf(token_begins, new_capacity);
    }
    token_types[token_count] = token_type;
    token_begins[token_count] = begin;
    token_count++;
  }
}
