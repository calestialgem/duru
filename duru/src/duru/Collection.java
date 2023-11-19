package duru;

import java.util.function.Function;

public interface Collection<Element> extends CollectionLike<Element> {
  @Override
  <U> Collection<U> transform(Function<Element, U> transformer);
}
