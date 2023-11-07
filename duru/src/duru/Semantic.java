package duru;

sealed interface Semantic {
  record Target() implements Semantic {}

  record Package() implements Semantic {}
}
