package duru;

import java.util.function.BiFunction;

public final class AcyclicCache<Key, Value> {
  public static <Key, Value> AcyclicCache<Key, Value> create(
    BiFunction<Object, Key, Value> function)
  {
    return new AcyclicCache<>(function, MapBuffer.create(), SetBuffer.create());
  }

  private final BiFunction<Object, Key, Value> function;
  private final MapBuffer<Key, Value>          cache;
  private final SetBuffer<Key>                 current;

  private AcyclicCache(
    BiFunction<Object, Key, Value> function,
    MapBuffer<Key, Value> cache,
    SetBuffer<Key> current)
  {
    this.function = function;
    this.cache    = cache;
    this.current  = current;
  }

  public Value get(Object subject, Key key) {
    var cached = cache.get(key);
    if (!cached.isEmpty())
      return cached.getFirst();
    if (current.contains(key))
      throw Diagnostic.error(subject, "cyclic dependencies `%s`", key);
    current.add(key);
    var value = function.apply(subject, key);
    current.remove(key);
    cache.add(key, value);
    return value;
  }

  public Map<Key, Value> getAll() {
    return cache.toMap();
  }
}
