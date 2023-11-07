package duru;

import java.util.List;

record LexedSource(Contents contents, List<Token> tokens) {
  public static LexedSource of(Contents contents, List<Token> tokens) {
    return new LexedSource(contents, List.copyOf(tokens));
  }
}
