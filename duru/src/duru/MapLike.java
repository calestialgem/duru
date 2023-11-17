package duru;

public interface MapLike<Key, Value> extends CollectionLike<Entry<Key, Value>> {
  boolean contains(Key key);
  Optional<Value> get(Key key);
}
