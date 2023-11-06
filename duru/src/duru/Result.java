package duru;

sealed interface Result<V, E> {
  record Success<V>(V value) implements Result<V, Object> {
    @Override
    public V orThrow() {
      return value;
    }
  }

  record Failure<E>(E error) implements Result<Object, E> {
    @Override
    public Object orThrow() {
      throw new UnsupportedOperationException(
        "Result is a failure with error `%s`!".formatted(error));
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
}
