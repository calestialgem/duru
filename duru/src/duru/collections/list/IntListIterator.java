package duru.collections.list;

import java.util.Iterator;

final class IntListIterator implements Iterator<Integer> {
  private final IntListLike list;
  private int               index;

  public IntListIterator(IntListLike list) {
    this.list = list;
    index     = 0;
  }

  @Override
  public boolean hasNext() {
    return index != list.length();
  }

  @Override
  public Integer next() {
    return nextAsInt();
  }

  public int nextAsInt() {
    return list.getAsInt(index);
  }
}
