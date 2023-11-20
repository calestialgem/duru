package duru;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.IntFunction;

public interface CollectionLike<Element> extends Iterable<Element> {
  int length();
  Element get(int index);
  <U> CollectionLike<U> transform(Function<Element, ? extends U> transformer);

  default Element getFirst() {
    return get(0);
  }

  default Element getLast() {
    return get(length() - 1);
  }

  default boolean isEmpty() {
    return length() == 0;
  }

  default Element[] toArray(IntFunction<Element[]> generator) {
    var elements = generator.apply(length());
    for (var i = 0; i < length(); i++) {
      elements[i] = get(i);
    }
    return elements;
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
