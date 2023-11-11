package duru;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

final class AcyclicCache<K, V> {
  private final Function<K, V> transformation;
  private final Map<K, V>      alreadyTransformed;
  private final SetBuffer<K>   currentTransformed;

  public AcyclicCache(Function<K, V> transformation) {
    this.transformation     = transformation;
    this.alreadyTransformed = new HashMap<>();
    this.currentTransformed = new SetBuffer<>();
  }

  public Result<V> get(K key) {
    if (alreadyTransformed.containsKey(key))
      return Result.success(alreadyTransformed.get(key));
    if (!currentTransformed.add(key))
      return Result.failure("Cyclic transformation of `%s`!", key);
    var value = transformation.apply(key);
    alreadyTransformed.put(key, value);
    currentTransformed.remove(key);
    return Result.success(value);
  }

  public Map<K, V> getAll() {
    return Map.copyOf(alreadyTransformed);
  }
}
