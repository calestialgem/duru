package duru;

import java.util.List;

record ParsedSource(LexedSource previous, List<Node.Declaration> declarations) {
  public static ParsedSource of(
    LexedSource previous,
    List<Node.Declaration> declarations)
  {
    return new ParsedSource(previous, List.copyOf(declarations));
  }
}
