package duru;

import java.nio.file.Path;
import java.util.Arrays;

public final class Lectics_Buffer {
  private byte[] kinds;
  private int[] begins;
  private int count;
  private int[] ends;
  private int end_count;

  public Lectics_Buffer() {
    kinds = new byte[0];
    begins = new int[0];
    ends = new int[0];
  }

  public void clear() {
    count = 0;
    end_count = 0;
  }

  public void add(Token kind, int begin) {
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

  public void add_varying(Token kind, int begin, int end) {
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

  public Lectics bake(Path path, String content) {
    return new Lectics(path, content, kinds, begins, count, ends, end_count);
  }
}
