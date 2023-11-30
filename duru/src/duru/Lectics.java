package duru;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Formatter;

public final class Lectics {
  public static final int OPENING_BRACE = 1;
  public static final int CLOSING_BRACE = 2;
  public static final int KEYWORD_ENTRYPOINT = 3;

  public static Lectics of(
    Path path,
    String contents,
    int[] types,
    int[] indices,
    int count)
  {
    return new Lectics(
      path,
      contents,
      Arrays.copyOf(types, count),
      Arrays.copyOf(indices, count));
  }

  private final Path path;
  private final String contents;
  private final int[] types;
  private final int[] indices;

  private Lectics(Path path, String contents, int[] types, int[] indices) {
    this.path = path;
    this.contents = contents;
    this.types = types;
    this.indices = indices;
  }

  @Override
  public int hashCode() {
    var result = 1;
    result = 31 * result + path.hashCode();
    result = 31 * result + contents.hashCode();
    result = 31 * result + Arrays.hashCode(types);
    result = 31 * result + Arrays.hashCode(indices);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj
      || obj instanceof Lectics other
        && path.equals(other.path)
        && contents.equals(other.contents)
        && Arrays.equals(types, other.types)
        && Arrays.equals(indices, other.indices);
  }

  @Override
  public String toString() {
    var string = new StringBuilder();
    try (var f = new Formatter(string)) {
      f
        .format(
          "'%s's lexical representation.%n%nHash: %X%n%n",
          path,
          hashCode());
      var line = 1;
      var column = 1;
      var index = 0;
      for (var i = 0; i < types.length; i++) {
        while (index != indices[i]) {
          if (contents.charAt(index) == '\n') {
            line++;
            column = 1;
          }
          else {
            column++;
          }
          index++;
        }
        f.format("%04d.%04d-", line, column);
        switch (types[i]) {
          case OPENING_BRACE -> f.format("%04d: punctuation `{`", column + 1);
          case CLOSING_BRACE -> f.format("%04d: punctuation `}`", column + 1);
          case KEYWORD_ENTRYPOINT ->
            f.format("%04d: keyword `entrypoint`", column + 10);
          default ->
            throw Diagnostic.failure("", "unknown token type %d", types[i]);
        }
        f.format("%n");
      }
    }
    return string.toString();
  }
}
