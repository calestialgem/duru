package duru;

import java.util.Iterator;

public interface Collection<Element> extends Iterable<Element> {
  int length();
  Element get(int index);

  default Element get() {
    return get(length() - 1);
  }

  default boolean isEmpty() {
    return length() == 0;
  }

  @Override
  default Iterator<Element> iterator() {
    return new Iterator<>() {
      private int index;

      @Override
      public boolean hasNext() {
        return index != length();
      }

      @Override
      public Element next() {
        var element = get(index);
        index++;
        return element;
      }
    };
  }
}
