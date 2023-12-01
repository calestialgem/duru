package duru;

import java.nio.file.Path;
import java.util.Arrays;

public final class Syntactics_Buffer {
  public static Syntactics_Buffer create() {
    return new Syntactics_Buffer(new byte[0], new int[0], 0);
  }

  private byte[] types;
  private int[] begins;
  private int count;

  private Syntactics_Buffer(byte[] types, int[] begins, int count) {
    this.types = types;
    this.begins = begins;
    this.count = count;
  }

  public void clear() {
    count = 0;
  }

  public void add_node(byte type, int begin) {
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

  public Syntactics bake(Path path, String contents) {
    return Syntactics.of(path, contents, types, begins, count);
  }
}
