package duru.collections.list;

import java.util.Iterator;

final class ObjectListIterator<E> implements Iterator<E> {
  private final ObjectListLike<E> list;
  private int                     index;

  public ObjectListIterator(ObjectListLike<E> list) {
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
