package duru;

import java.nio.file.Path;
import java.util.Arrays;

public final class Syntactics_Buffer {
  private byte[] kinds;
  private int[] begins;
  private int count;
  private int[] ends;
  private int end_count;

  public Syntactics_Buffer() {
    kinds = new byte[0];
    begins = new int[0];
    ends = new int[0];
  }

  public void clear() {
    count = 0;
    end_count = 0;
  }

  public void add(Node kind, int begin) {
    if (count == kinds.length) {
      var new_capacity = count * 2;
      if (new_capacity == 0)
        new_capacity = 1;
      kinds = Arrays.copyOf(kinds, new_capacity);
      begins = Arrays.copyOf(begins, new_capacity);
    }
    kinds[count] = (byte) kind.ordinal();
    begins[count] = begin;
    count++;
  }

  public void add_varying(Node kind, int begin, int end) {
    add(kind, begin);
    if (end_count == ends.length) {
      var new_capacity = end_count * 2;
      if (new_capacity == 0)
        new_capacity = 1;
      ends = Arrays.copyOf(ends, new_capacity);
    }
    ends[end_count] = end;
    end_count++;
  }

  public Syntactics bake(Path path, String content) {
    return new Syntactics(path, content, kinds, begins, count, ends, end_count);
  }
}
