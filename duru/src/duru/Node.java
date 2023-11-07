package duru;

sealed interface Node {
  record Declaration() implements Node {}
}
