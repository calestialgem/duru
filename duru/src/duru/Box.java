package duru;

sealed interface Box<V> permits Present, Absent {
  static <V> Box<V> present(V value) {
    return new Present<>(value);
  }

  @SuppressWarnings("unchecked")
  static <V> Box<V> absent() {
    return new Absent();
  }
}
