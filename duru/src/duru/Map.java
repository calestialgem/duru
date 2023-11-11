package duru;

record Map<K, V>(List<K> keys, List<V> values, List<Integer> buckets)
  implements MapLike<K, V>
{
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
}
