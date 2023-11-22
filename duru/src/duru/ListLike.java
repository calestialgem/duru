package duru;

import java.util.function.Function;

public interface ListLike<Element> extends CollectionLike<Element> {
  ListLike<Element> sublist(int begin, int end);
  @Override
  <U> ListLike<U> transform(Function<Element, ? extends U> transformer);
}
