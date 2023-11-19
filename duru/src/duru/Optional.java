package duru;

import java.util.function.Function;
import java.util.function.Supplier;

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
  public String toString() {
    var string = new StringBuilder();
    string.append('[');
    if (!isEmpty()) {
      string.append(getFirst());
    }
    string.append(']');
    return string.toString();
  }

  @Override
  public <U> Optional<U> transform(Function<Value, ? extends U> transformer) {
    if (isEmpty())
      return absent();
    return present(transformer.apply(value));
  }

  public Value getOrElse(Value fallback) {
    if (isEmpty())
      return fallback;
    return value;
  }

  public Value getOrElse(Supplier<? extends Value> fallback) {
    if (isEmpty())
      return fallback.get();
    return value;
  }
}
