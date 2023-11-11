package duru;

sealed interface ListLike<E> permits List, ListBuffer {
  int length();
  E get(int index);
}
