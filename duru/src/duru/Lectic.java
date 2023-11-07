package duru;

import java.util.List;

sealed interface Lectic {
  record Source(Contents contents, List<Token> tokens) implements Lectic {
    public static Source of(Contents contents, List<Token> tokens) {
      return new Source(contents, List.copyOf(tokens));
    }
  }

  sealed interface Token extends Lectic {}

  record EndOfFile() implements Token {}
}
