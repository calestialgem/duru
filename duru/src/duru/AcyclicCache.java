package duru;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

final class AcyclicCache<K, V> {
  private final Function<K, V> transformation;
  private final Map<K, V>      alreadyTransformed;
  private final Set<K>         currentTransformed;

  public AcyclicCache(Function<K, V> transformation) {
    this.transformation     = transformation;
    this.alreadyTransformed = new HashMap<>();
    this.currentTransformed = new HashSet<>();
  }

  public Result<V> get(K key) {
    if (alreadyTransformed.containsKey(key))
      return Result.success(alreadyTransformed.get(key));
    if (currentTransformed.contains(key))
      return Result.failure("Cyclic transformation of `%s`!", key);
    currentTransformed.add(key);
    var value = transformation.apply(key);
    alreadyTransformed.put(key, value);
    currentTransformed.remove(key);
    return Result.success(value);
  }

  public Map<K, V> getAll() {
    return Map.copyOf(alreadyTransformed);
  }
}
