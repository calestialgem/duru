package duru;

public final class Map<Key, Value>
  implements
  Collection<Map.Entry<Key, Value>>
{
  public record Entry<Key, Value>(Key key, Value value) {}

  private final List<Key>     keys;
  private final List<Value>   values;
  private final List<Integer> buckets;

  private Map(List<Key> keys, List<Value> values, List<Integer> buckets) {
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
}
