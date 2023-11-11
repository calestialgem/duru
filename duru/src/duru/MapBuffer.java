package duru;

final class MapBuffer<K, V> implements MapLike<K, V> {
  private final ListBuffer<K>       keys;
  private final ListBuffer<V>       values;
  private final ListBuffer<Integer> buckets;

  public MapBuffer() {
    keys    = new ListBuffer<>();
    values  = new ListBuffer<>();
    buckets = new ListBuffer<>();
  }

  @Override
  public int length() {
    return keys.length();
  }

  @Override
  public Box<V> get(K key) {
    var expectedBucket = key.hashCode();
    for (var i = 0; i < buckets.length(); i++) {
      var bucket = (expectedBucket + i) % buckets.length();
      var index  = buckets.get(bucket);
      if (index == -1)
        break;
      var mapped = keys.get(index);
      if (mapped.equals(key))
        return Box.present(values.get(index));
    }
    return Box.absent();
  }

  public Box<V> add(K key, V value) {
    var expectedBucket = key.hashCode();
    for (var i = 0; i < buckets.length(); i++) {
      var bucket = (expectedBucket + i) % buckets.length();
      var index  = buckets.get(bucket);
      if (index == -1) {
        buckets.set(bucket, keys.length());
        keys.add(key);
        values.add(value);
        return Box.absent();
      }
      var mapped = keys.get(index);
      if (mapped.equals(key)) {
        return Box.present(values.set(index, value));
      }
    }
    keys.add(key);
    values.add(value);
    rehash();
    return Box.absent();
  }

  public Map<K, V> toMap() {
    return new Map<>(keys.toList(), values.toList(), buckets.toList());
  }

  private void rehash() {
    buckets.clear();
    buckets.add(-1, keys.length() * 2);
    for (var m = 0; m < keys.length(); m++) {
      var expectedBucket = keys.get(m).hashCode();
      for (var i = 0; i < buckets.length(); i++) {
        var bucket = (expectedBucket + i) % buckets.length();
        var index  = buckets.get(bucket);
        if (index == -1) {
          buckets.set(bucket, m);
          break;
        }
      }
    }
  }
}
