package duru;

sealed interface MapLike<K, V> permits Map, MapBuffer {
  int length();
  Box<V> get(K key);
}
