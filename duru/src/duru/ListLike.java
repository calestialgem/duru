package duru;

sealed interface ListLike<E> permits List, ListBuffer {
  int length();
  E get(int index);

  default boolean isEmpty() {
    return length() == 0;
  }
}
