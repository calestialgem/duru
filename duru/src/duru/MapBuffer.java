package duru;

import java.util.function.BiFunction;
import java.util.function.Function;

public final class MapBuffer<Key, Value> implements MapLike<Key, Value> {
  public static <Key, Value> MapBuffer<Key, Value> create() {
    return new MapBuffer<>(
      ListBuffer.create(),
      ListBuffer.create(),
      ListBuffer.create());
  }

  private final ListBuffer<Key>     keys;
  private final ListBuffer<Value>   values;
  private final ListBuffer<Integer> buckets;

  private MapBuffer(
    ListBuffer<Key> keys,
    ListBuffer<Value> values,
    ListBuffer<Integer> buckets)
  {
    this.keys    = keys;
    this.values  = values;
    this.buckets = buckets;
  }

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
  public <U> MapBuffer<Key, U> transformValues(Function<Value, U> transformer) {
    return new MapBuffer<>(
      keys.copy(),
      values.transform(transformer),
      buckets.copy());
  }

  @Override
  public <U> ListBuffer<U> transform(BiFunction<Key, Value, U> transformer) {
    var list = ListBuffer.<U>create();
    for (var i = 0; i < length(); i++) {
      list.add(transformer.apply(keys.get(i), values.get(i)));
    }
    return list;
  }

  @Override
  public <U> ListBuffer<U> transform(
    Function<Entry<Key, Value>, U> transformer)
  {
    var list = ListBuffer.<U>create();
    for (var entry : this) {
      list.add(transformer.apply(entry));
    }
    return list;
  }

  public Map<Key, Value> toMap() {
    rehash();
    return new Map<>(keys.toList(), values.toList(), buckets.toList());
  }

  public MapBuffer<Key, Value> copy() {
    return new MapBuffer<>(keys.copy(), values.copy(), buckets.copy());
  }

  public boolean add(Key key, Value value) {
    var hash = key.hashCode();
    for (var i = 0; i < buckets.length(); i++) {
      var bucket = Math.floorMod(hash + i, buckets.length());
      var index  = buckets.get(bucket);
      if (index == -1) {
        buckets.set(bucket, keys.length());
        keys.add(key);
        values.add(value);
        return true;
      }
      if (key.equals(keys.get(index))) {
        return false;
      }
    }
    keys.add(key);
    values.add(value);
    rehash();
    return true;
  }

  public boolean remove(Key key) {
    var hash = key.hashCode();
    for (var i = 0; i < buckets.length(); i++) {
      var bucket = Math.floorMod(hash + i, buckets.length());
      var index  = buckets.get(bucket);
      if (index == -1) {
        return false;
      }
      if (key.equals(keys.get(index))) {
        keys.remove(index);
        values.remove(index);
        rehash();
        return true;
      }
    }
    return false;
  }

  private void rehash() {
    buckets.clear();
    buckets.fill(-1, keys.length() * 2);
    for (var k = 0; k < keys.length(); k++) {
      var key  = keys.get(k);
      var hash = key.hashCode();
      for (var i = 0; i < buckets.length(); i++) {
        var bucket = Math.floorMod(hash + i, buckets.length());
        var index  = buckets.get(bucket);
        if (index == -1) {
          buckets.set(bucket, k);
          break;
        }
      }
    }
  }
}
