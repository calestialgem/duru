package duru;

sealed interface Token {
  record EndOfFile() implements Token {}
}
