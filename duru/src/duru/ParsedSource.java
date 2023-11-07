package duru;

import java.util.List;

record ParsedSource(Lectics lectics, List<Node.Declaration> declarations) {
  public static ParsedSource of(
    Lectics lectics,
    List<Node.Declaration> declarations)
  {
    return new ParsedSource(lectics, List.copyOf(declarations));
  }
}
