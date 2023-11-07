package duru;

import java.util.List;

record LexedSource(LoadedSource previous, List<Token> tokens) {
  public static LexedSource of(LoadedSource previous, List<Token> tokens) {
    return new LexedSource(previous, List.copyOf(tokens));
  }
}
