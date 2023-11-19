package duru;

@FunctionalInterface
public interface Accessor<K, V> {
  V access(Object subject, K key);
}
