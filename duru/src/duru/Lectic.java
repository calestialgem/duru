package duru;

sealed interface Lectic {
  record Source(Contents contents, List<Token> tokens) implements Lectic {}

  sealed interface Token extends Lectic {}

  record EndOfFile() implements Token {}
}
