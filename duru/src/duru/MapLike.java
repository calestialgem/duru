package duru;

import java.util.function.BiFunction;
import java.util.function.Function;

public interface MapLike<Key, Value> extends CollectionLike<Entry<Key, Value>> {
  boolean contains(Key key);
  Optional<Value> get(Key key);
  <U> MapLike<Key, U> transformValues(Function<Value, ? extends U> transformer);
  <U> ListLike<U> transform(BiFunction<Key, Value, ? extends U> transformer);
  @Override
  <U> ListLike<U> transform(
    Function<Entry<Key, Value>, ? extends U> transformer);
}
