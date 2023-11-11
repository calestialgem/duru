package duru;

sealed interface SetLike<M> permits Set, SetBuffer {
  int length();
  boolean contains(M member);
}
