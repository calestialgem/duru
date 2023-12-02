package duru;

import java.nio.file.Path;
import java.util.Arrays;

public final class Lectics {
  public static final byte OPENING_BRACE = 0x01;
  public static final byte CLOSING_BRACE = 0x02;
  public static final byte KEYWORD_ENTRYPOINT = 0x03;
  public final Path path;
  public final String contents;
  private final byte[] types;
  private final int[] begins;

  public Lectics(
    Path path,
    String contents,
    byte[] types,
    int[] begins,
    int count)
  {
    this.path = path;
    this.contents = contents;
    this.types = Arrays.copyOf(types, count);
    this.begins = Arrays.copyOf(begins, count);
  }

  public int token_count() {
    return types.length;
  }

  public byte type_of(int token) {
    return types[token];
  }

  public int begin_of(int token) {
    return begins[token];
  }

  public Object subject_of(int token) {
    var index = 0;
    var line = 1;
    var column = 1;
    while (index != begin_of(token)) {
      if (contents.charAt(index) != '\n') {
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
    switch (type_of(token)) {
      case OPENING_BRACE -> {
        return "{".length();
      }
      case CLOSING_BRACE -> {
        return "}".length();
      }
      case KEYWORD_ENTRYPOINT -> {
        return "entrypoint".length();
      }
      default -> throw unknown(token);
    }
  }

  public String explain(int token) {
    switch (type_of(token)) {
      case OPENING_BRACE -> {
        return "punctuation `{`";
      }
      case CLOSING_BRACE -> {
        return "punctuation `}`";
      }
      case KEYWORD_ENTRYPOINT -> {
        return "keyword `entrypoint`";
      }
      default -> throw unknown(token);
    }
  }

  @Override
  public int hashCode() {
    var result = 1;
    result = 31 * result + path.hashCode();
    result = 31 * result + contents.hashCode();
    result = 31 * result + Arrays.hashCode(types);
    result = 31 * result + Arrays.hashCode(begins);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj
      || obj instanceof Lectics other
        && path.equals(other.path)
        && contents.equals(other.contents)
        && Arrays.equals(types, other.types)
        && Arrays.equals(begins, other.begins);
  }

  private RuntimeException unknown(int token) {
    var begin = begin_of(token);
    var index = 0;
    var line = 1;
    var column = 1;
    while (index != begin) {
      if (contents.charAt(index) != '\n') {
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
        "unknown token type %d",
        type_of(token));
  }
}
