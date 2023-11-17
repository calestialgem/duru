package duru;

public sealed interface Optional<Value> extends Collection<Value>
  permits
  Present,
  Absent
{
  public static <Value> Optional<Value> present(Value value) {
    return new Present<>(value);
  }

  @SuppressWarnings("unchecked")
  public static <Value> Optional<Value> absent() {
    return (Optional<Value>) new Absent();
  }
}
