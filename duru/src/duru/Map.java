package duru;

import java.util.function.BiFunction;
import java.util.function.Function;

public record Map<Key, Value>(
  List<Key> keys,
  List<Value> values,
  List<Integer> buckets)
  implements MapLike<Key, Value>, Collection<Entry<Key, Value>>
{
  @Override
  public int length() {
    return keys.length();
  }

  @Override
  public Entry<Key, Value> get(int index) {
    return new Entry<>(keys.get(index), values.get(index));
  }

  @Override
  public boolean contains(Key key) {
    var hash = key.hashCode();
    for (var i = 0; i < buckets.length(); i++) {
      var bucket = Math.floorMod(hash + i, buckets.length());
      var index  = buckets.get(bucket);
      if (index == -1) {
        return false;
      }
      if (key.equals(keys.get(index))) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Optional<Value> get(Key key) {
    var hash = key.hashCode();
    for (var i = 0; i < buckets.length(); i++) {
      var bucket = Math.floorMod(hash + i, buckets.length());
      var index  = buckets.get(bucket);
      if (index == -1) {
        return Optional.absent();
      }
      if (key.equals(keys.get(index))) {
        return Optional.present(values.get(index));
      }
    }
    return Optional.absent();
  }

  @Override
  public <U> Map<Key, U> transformValues(Function<Value, U> transformer) {
    return new Map<>(keys, values.transform(transformer), buckets);
  }

  @Override
  public <U> List<U> transform(BiFunction<Key, Value, U> transformer) {
    var list = ListBuffer.<U>create();
    for (var i = 0; i < length(); i++) {
      list.add(transformer.apply(keys.get(i), values.get(i)));
    }
    return list.toList();
  }

  @Override
  public <U> List<U> transform(Function<Entry<Key, Value>, U> transformer) {
    var list = ListBuffer.<U>create();
    for (var entry : this) {
      list.add(transformer.apply(entry));
    }
    return list.toList();
  }

  @Override
  public String toString() {
    var string = new StringBuilder();
    string.append('{');
    if (!keys.isEmpty()) {
      string.append(keys.getFirst());
      string.append(':');
      string.append(' ');
      string.append(values.getFirst());
      for (var i = 1; i < keys.length(); i++) {
        string.append(',');
        string.append(' ');
        string.append(keys.get(i));
        string.append(':');
        string.append(' ');
        string.append(values.get(i));
      }
    }
    string.append('}');
    return string.toString();
  }
}
