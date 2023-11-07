package duru;

import java.util.Iterator;

record List<E>(E[] elements) implements Iterable<E> {
  public int length() {
    return elements.length;
  }

  public E get(int index) {
    return elements[index];
  }

  @Override
  public Iterator<E> iterator() {
    return new ListIterator<>(this);
  }
}
