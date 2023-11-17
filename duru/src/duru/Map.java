package duru;

public record Map<Key, Value>(
  List<Key> keys,
  List<Value> values,
  List<Integer> buckets) implements Collection<Map.Entry<Key, Value>>
{
  public record Entry<Key, Value>(Key key, Value value) {}

  @Override
  public int length() {
    return keys.length();
  }

  @Override
  public Entry<Key, Value> get(int index) {
    return new Entry<>(keys.get(index), values.get(index));
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
