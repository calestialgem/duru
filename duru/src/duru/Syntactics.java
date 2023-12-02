package duru;

import java.nio.file.Path;
import java.util.Arrays;

public final class Syntactics {
  public final Path path;
  public final String content;
  private final byte[] kinds;
  private final int[] begins;
  private final int[] ends;

  public Syntactics(
    Path path,
    String content,
    byte[] kinds,
    int[] begins,
    int count,
    int[] ends,
    int end_count)
  {
    this.path = path;
    this.content = content;
    this.kinds = Arrays.copyOf(kinds, count);
    this.begins = Arrays.copyOf(begins, count);
    this.ends = Arrays.copyOf(ends, end_count);
  }

  public int count() {
    return kinds.length;
  }

  public Node kind(int index) {
    return Node.values()[kinds[index]];
  }

  public int begin(int index) {
    return begins[index];
  }

  public int end(int end_index) {
    return ends[end_index];
  }

  @Override
  public int hashCode() {
    var result = 1;
    result = 31 * result + path.hashCode();
    result = 31 * result + content.hashCode();
    result = 31 * result + Arrays.hashCode(kinds);
    result = 31 * result + Arrays.hashCode(begins);
    result = 31 * result + Arrays.hashCode(ends);
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    return this == obj
      || obj instanceof Syntactics other
        && path.equals(other.path)
        && content.equals(other.content)
        && Arrays.equals(kinds, other.kinds)
        && Arrays.equals(begins, other.begins)
        && Arrays.equals(begins, other.ends);
  }
}
