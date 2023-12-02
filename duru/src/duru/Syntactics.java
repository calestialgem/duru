package duru;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Formatter;

public final class Syntactics {
  public static final byte ENTRYPOINT_DECLARATION = 0x01;
  public static final byte BLOCK_STATEMENT_BEGIN = 0x02;
  public static final byte BLOCK_STATEMENT_END = 0x03;

  public static boolean is_declaration(byte type) {
    return type == ENTRYPOINT_DECLARATION;
  }

  public static Syntactics of(
    Path path,
    String contents,
    byte[] types,
    int[] begins,
    int count)
  {
    return new Syntactics(
      path,
      contents,
      Arrays.copyOf(types, count),
      Arrays.copyOf(begins, count));
  }

  public final Path path;
  public final String contents;
  private final byte[] types;
  private final int[] begins;

  private Syntactics(Path path, String contents, byte[] types, int[] begins) {
    this.path = path;
    this.contents = contents;
    this.types = types;
    this.begins = begins;
  }

  public int node_count() {
    return types.length;
  }

  public byte type_of(int node) {
    return types[node];
  }

  public int begin_of(int node) {
    return begins[node];
  }

  public String text_of(int node) {
    return contents.substring(begin_of(node), begin_of(node) + length_of(node));
  }

  public Object subject_of(int node) {
    var index = 0;
    var line = 1;
    var column = 1;
    while (index != begin_of(node)) {
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
      .formatted(path, line, column, column + length_of(node));
  }

  public int length_of(int node) {
    switch (type_of(node)) {
      case ENTRYPOINT_DECLARATION -> {
        return "entrypoint".length();
      }
      case BLOCK_STATEMENT_BEGIN -> {
        return "{".length();
      }
      case BLOCK_STATEMENT_END -> {
        return "}".length();
      }
      default -> throw unknown(node);
    }
  }

  public String explain(int node) {
    switch (type_of(node)) {
      case ENTRYPOINT_DECLARATION -> {
        return "entrypoint declaration";
      }
      case BLOCK_STATEMENT_BEGIN -> {
        return "block statement begin";
      }
      case BLOCK_STATEMENT_END -> {
        return "block statement end";
      }
      default -> throw unknown(node);
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
      || obj instanceof Syntactics other
        && path.equals(other.path)
        && contents.equals(other.contents)
        && Arrays.equals(types, other.types)
        && Arrays.equals(begins, other.begins);
  }

  @Override
  public String toString() {
    var string = new StringBuilder();
    try (var f = new Formatter(string)) {
      f
        .format(
          "'%s's syntactical representation.%n%nHash: %X%n%n",
          path,
          hashCode());
      for (var node = 0; node < node_count(); node++) {
        var line = 1;
        var column = 1;
        var index = 0;
        while (index != begin_of(node)) {
          if (contents.charAt(index) == '\n') {
            line++;
            column = 1;
          }
          else {
            column++;
          }
          index++;
        }
        f
          .format(
            "%04d: %04d.%04d-%04d: %s%n",
            node,
            line,
            column,
            column + length_of(node),
            explain(node));
      }
    }
    return string.toString();
  }

  private RuntimeException unknown(int node) {
    var begin = begin_of(node);
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
        "unknown node type %d",
        type_of(node));
  }
}
