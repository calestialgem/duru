package duru;

import java.util.function.Function;
import java.util.function.Supplier;

sealed interface Result<V> {
  record Success<V>(V value) implements Result<V> {
    @Override
    public V orThrow() {
      return value;
    }

    @Override
    public <U> Result<U> perform(Function<V, Result<U>> procedure) {
      return procedure.apply(value);
    }
  }

  record Failure(String error) implements Result<Object> {
    @Override
    public Object orThrow() {
      throw new RuntimeException(error);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> Result<U> perform(Function<Object, Result<U>> procedure) {
      return (Result<U>) this;
    }
  }

  static <V> Result<V> success(V value) {
    return new Success<>(value);
  }

  @SuppressWarnings("unchecked")
  static <V> Result<V> failure(String format, Object... arguments) {
    return (Result<V>) new Failure(format.formatted(arguments));
  }

  static <V> Result<V> perform(Supplier<Result<V>> procedure) {
    return procedure.get();
  }

  V orThrow();
  <U> Result<U> perform(Function<V, Result<U>> procedure);
}
