package duru;

import java.util.function.Function;

public final class AcyclicCache<Key, Value> {
  public static <Key, Value> AcyclicCache<Key, Value> create(
    Function<Key, Value> function)
  {
    return new AcyclicCache<>(function, MapBuffer.create(), SetBuffer.create());
  }

  private final Function<Key, Value>  function;
  private final MapBuffer<Key, Value> cache;
  private final SetBuffer<Key>        current;

  private AcyclicCache(
    Function<Key, Value> function,
    MapBuffer<Key, Value> cache,
    SetBuffer<Key> current)
  {
    this.function = function;
    this.cache    = cache;
    this.current  = current;
  }

  public Value map(Key key) {
    var cached = cache.get(key);
    if (!cached.isEmpty())
      return cached.getFirst();
    if (current.contains(key))
      throw Subject.error("cyclic dependencies `%s`", key);
    current.add(key);
    var value = function.apply(key);
    current.remove(key);
    cache.add(key, value);
    return value;
  }

  public Map<Key, Value> getAll() {
    return cache.toMap();
  }
}
