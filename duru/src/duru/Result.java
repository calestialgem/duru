package duru;

import java.util.function.Function;

sealed interface Result<V, E> {
  record Success<V>(V value) implements Result<V, Object> {
    @Override
    public V orThrow() {
      return value;
    }

    @Override
    public <U> Result<U, Object> then(
      Function<V, Result<U, Object>> procedure)
    {
      return procedure.apply(value);
    }
  }

  record Failure<E>(E error) implements Result<Object, E> {
    @Override
    public Object orThrow() {
      throw new UnsupportedOperationException(
        "Result is a failure with error `%s`!".formatted(error));
    }

    @Override
    @SuppressWarnings("unchecked")
    public <U> Result<U, E> then(Function<Object, Result<U, E>> procedure) {
      return (Result<U, E>) this;
    }
  }

  @SuppressWarnings("unchecked")
  static <V, E> Result<V, E> success(V value) {
    return (Result<V, E>) new Success<>(value);
  }

  @SuppressWarnings("unchecked")
  static <V, E> Result<V, E> failure(E error) {
    return (Result<V, E>) new Failure<>(error);
  }

  V orThrow();
  <U> Result<U, E> then(Function<V, Result<U, E>> procedure);
}
