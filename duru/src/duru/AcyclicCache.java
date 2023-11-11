package duru;

import java.util.function.Function;

final class AcyclicCache<K, V> {
  private final Function<K, V>  transformation;
  private final MapBuffer<K, V> alreadyTransformed;
  private final SetBuffer<K>    currentTransformed;

  public AcyclicCache(Function<K, V> transformation) {
    this.transformation = transformation;
    alreadyTransformed  = new MapBuffer<>();
    currentTransformed  = new SetBuffer<>();
  }

  public Result<V> get(K key) {
    if (alreadyTransformed.get(key) instanceof Present(var value)) {
      return Result.success(value);
    }
    if (!currentTransformed.add(key))
      return Result.failure("Cyclic transformation of `%s`!", key);
    var value = transformation.apply(key);
    alreadyTransformed.add(key, value);
    currentTransformed.remove(key);
    return Result.success(value);
  }

  public Map<K, V> getAll() {
    return alreadyTransformed.toMap();
  }
}
