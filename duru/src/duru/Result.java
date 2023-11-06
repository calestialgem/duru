package duru;

import java.util.function.Function;

sealed interface Result<V> {
  record Success<V>(V value) implements Result<V> {
    @Override
    public V orThrow() {
      return value;
    }

    @Override
    public <U> Result<U> then(Function<V, Result<U>> procedure) {
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
    public <U> Result<U> then(Function<Object, Result<U>> procedure) {
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

  V orThrow();
  <U> Result<U> then(Function<V, Result<U>> procedure);
}
