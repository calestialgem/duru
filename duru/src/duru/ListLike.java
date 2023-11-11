package duru;

sealed interface ListLike<E> permits List, ListBuffer {}
