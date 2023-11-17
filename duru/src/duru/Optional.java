package duru;

public final class Optional<Value> {
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

  public boolean isEmpty() {
    return value == null;
  }

  public Value get() {
    return value;
  }
}
