package duru;

import java.nio.file.Path;
import java.util.Arrays;

public final class Lectics {
  public final Path path;
  public final String content;
  private final byte[] kinds;
  private final int[] begins;

  public Lectics(
    Path path,
    String content,
    byte[] kinds,
    int[] begins,
    int count)
  {
    this.path = path;
    this.content = content;
    this.kinds = Arrays.copyOf(kinds, count);
    this.begins = Arrays.copyOf(begins, count);
  }

  public int token_count() {
    return kinds.length;
  }

  public Token kind_of(int token) {
    return Token.values()[kinds[token]];
  }

  public int begin_of(int token) {
    return begins[token];
  }

  public Object subject_of(int token) {
    var index = 0;
    var line = 1;
    var column = 1;
    while (index != begin_of(token)) {
      if (content.charAt(index) != '\n') {
        column++;
      }
      else {
        line++;
        column = 1;
      }
      index++;
    }
    return "%s:%d.%d-%d"
      .formatted(path, line, column, column + length_of(token));
  }

  public int length_of(int token) {
    switch (kind_of(token)) {
      case Token.OPENING_BRACE -> {
        return "{".length();
      }
      case Token.CLOSING_BRACE -> {
        return "}".length();
      }
      case Token.ENTRYPOINT -> {
        return "entrypoint".length();
      }
      default -> throw unknown(token);
    }
  }

  public String explain(int token) {
    switch (kind_of(token)) {
      case Token.OPENING_BRACE -> {
        return "punctuation `{`";
      }
      case Token.CLOSING_BRACE -> {
        return "punctuation `}`";
      }
      case Token.ENTRYPOINT -> {
        return "keyword `entrypoint`";
      }
      default -> throw unknown(token);
    }
  }

  @Override
  public int hashCode() {
    var result = 1;
    result = 31 * result + path.hashCode();
    result = 31 * result + content.hashCode();
    result = 31 * result + Arrays.hashCode(kinds);
    result = 31 * result + Arrays.hashCode(begins);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj
      || obj instanceof Lectics other
        && path.equals(other.path)
        && content.equals(other.content)
        && Arrays.equals(kinds, other.kinds)
        && Arrays.equals(begins, other.begins);
  }

  private RuntimeException unknown(int token) {
    var begin = begin_of(token);
    var index = 0;
    var line = 1;
    var column = 1;
    while (index != begin) {
      if (content.charAt(index) != '\n') {
        column++;
      }
      else {
        line++;
        column = 1;
      }
      index++;
    }
    return Diagnostic
      .failure(
        "%s:%d.%d".formatted(path, line, column),
        "unknown token kind %s",
        kind_of(token));
  }
}
