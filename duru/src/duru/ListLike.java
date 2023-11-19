package duru;

import java.util.function.Function;

public interface ListLike<Element> extends CollectionLike<Element> {
  @Override
  <U> ListLike<U> transform(Function<Element, U> transformer);
}
