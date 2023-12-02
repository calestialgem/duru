package duru;

import java.nio.file.Path;
import java.util.Arrays;

public final class Syntactics {
  public final Path path;
  public final String content;
  private final byte[] kinds;
  private final int[] begins;

  public Syntactics(
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

  public int node_count() {
    return kinds.length;
  }

  public Node kind_of(int node) {
    return Node.values()[kinds[node]];
  }

  public int begin_of(int node) {
    return begins[node];
  }

  public String text_of(int node) {
    return content.substring(begin_of(node), begin_of(node) + length_of(node));
  }

  public Object subject_of(int node) {
    var index = 0;
    var line = 1;
    var column = 1;
    while (index != begin_of(node)) {
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
      .formatted(path, line, column, column + length_of(node));
  }

  public int length_of(int node) {
    switch (kind_of(node)) {
      case Node.ENTRYPOINT -> {
        return "entrypoint".length();
      }
      case Node.BLOCK_BEGIN -> {
        return "{".length();
      }
      case Node.BLOCK_END -> {
        return "}".length();
      }
      default -> throw unknown(node);
    }
  }

  public String explain(int node) {
    switch (kind_of(node)) {
      case Node.ENTRYPOINT -> {
        return "entrypoint declaration";
      }
      case Node.BLOCK_BEGIN -> {
        return "block statement begin";
      }
      case Node.BLOCK_END -> {
        return "block statement end";
      }
      default -> throw unknown(node);
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
      || obj instanceof Syntactics other
        && path.equals(other.path)
        && content.equals(other.content)
        && Arrays.equals(kinds, other.kinds)
        && Arrays.equals(begins, other.begins);
  }

  private RuntimeException unknown(int node) {
    var begin = begin_of(node);
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
        "unknown node kind %s",
        kind_of(node));
  }
}
