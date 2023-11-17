package duru;

import java.util.Objects;

public record Optional<Value>(Value value) implements Collection<Value> {
  public static <Value> Optional<Value> present(Value value) {
    return new Optional<>(value);
  }

  public static <Value> Optional<Value> absent() {
    return new Optional<>(null);
  }

  @Override
  public int length() {
    return value == null ? 0 : 1;
  }

  @Override
  public Value get(int index) {
    return value;
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }

  @Override
  public boolean equals(Object other) {
    if (this == other)
      return true;
    if (!(other instanceof Optional otherOptional))
      return false;
    return Objects.equals(value, otherOptional.value);
  }

  @Override
  public String toString() {
    return Objects.toString(value);
  }
}
