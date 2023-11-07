package duru;

import java.util.Iterator;

final class ListIterator<E> implements Iterator<E> {
  private final List<E> list;
  private int           index;

  public ListIterator(List<E> list) {
    this.list = list;
    index     = 0;
  }

  @Override
  public boolean hasNext() {
    return index != list.length();
  }

  @Override
  public E next() {
    return list.get(index);
  }
}
