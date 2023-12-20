package duru;

import java.nio.file.Path;
import java.util.Arrays;

public final class Semantics {
  private final Path[] paths;
  private final String[] contents;
  private final int[] source_begins;
  private final byte[] types;
  private final int[] begins;
  private final String[] names;

  public Semantics(
    Path[] paths,
    String[] contents,
    int[] source_begins,
    byte[] types,
    int[] begins,
    int source_count,
    int count,
    String[] names,
    int name_count)
  {
    this.paths = Arrays.copyOf(paths, source_count, Path[].class);
    this.contents = Arrays.copyOf(contents, source_count, String[].class);
    this.source_begins = Arrays.copyOf(source_begins, source_count);
    this.types = Arrays.copyOf(types, count);
    this.begins = Arrays.copyOf(begins, count);
    this.names = Arrays.copyOf(names, name_count);
  }

  @Override
  public int hashCode() {
    var result = 1;
    result = 31 * result + Arrays.hashCode(paths);
    result = 31 * result + Arrays.hashCode(contents);
    result = 31 * result + Arrays.hashCode(source_begins);
    result = 31 * result + Arrays.hashCode(types);
    result = 31 * result + Arrays.hashCode(begins);
    result = 31 * result + Arrays.hashCode(names);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj
      || obj instanceof Semantics other
        && Arrays.equals(paths, other.paths)
        && Arrays.equals(contents, other.contents)
        && Arrays.equals(source_begins, other.source_begins)
        && Arrays.equals(types, other.types)
        && Arrays.equals(begins, other.begins)
        && Arrays.equals(names, other.names);
  }
}
