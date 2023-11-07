package duru;

import java.util.List;

record Lectics(Contents contents, List<Token> tokens) {
  public static Lectics of(Contents contents, List<Token> tokens) {
    return new Lectics(contents, List.copyOf(tokens));
  }
}
