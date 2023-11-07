package duru;

import java.util.Arrays;
import java.util.Iterator;

record List<E>(E[] elements) implements Iterable<E> {
  @SafeVarargs
  public static <E> List<E> of(E... elements) {
    return new List<>(Arrays.copyOf(elements, elements.length));
  }

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
