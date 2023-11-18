package duru;

@FunctionalInterface
public interface Accessor<K, V> {
  V access(K key);
}
