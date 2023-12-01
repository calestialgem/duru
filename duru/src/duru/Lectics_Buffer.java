package duru;

import java.nio.file.Path;
import java.util.Arrays;

public final class Lectics_Buffer {
  public static Lectics_Buffer create() {
    return new Lectics_Buffer(new byte[0], new int[0], 0);
  }

  private byte[] types;
  private int[] begins;
  private int count;

  private Lectics_Buffer(byte[] types, int[] begins, int count) {
    this.types = types;
    this.begins = begins;
    this.count = count;
  }

  public void clear() {
    count = 0;
  }

  public void add_token(byte type, int begin) {
    if (count == types.length) {
      var capacity = types.length * 2;
      if (capacity == 0)
        capacity = 1;
      types = Arrays.copyOf(types, capacity);
      begins = Arrays.copyOf(begins, capacity);
    }
    types[count] = type;
    begins[count] = begin;
    count++;
  }

  public Lectics bake(Path path, String contents) {
    return Lectics.of(path, contents, types, begins, count);
  }
}
