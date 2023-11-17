package duru;

public final class Optional<Value> implements Collection<Value> {
  public static <Value> Optional<Value> present(Value value) {
    return new Optional<>(value);
  }

  public static <Value> Optional<Value> absent() {
    return new Optional<>(null);
  }

  private final Value value;

  private Optional(Value value) {
    this.value = value;
  }

  @Override
  public int length() {
    return value == null ? 0 : 1;
  }

  @Override
  public Value get(int index) {
    return value;
  }
}
