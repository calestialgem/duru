package duru;

import java.util.List;

record Syntactics(Lectic.Source lectics, List<Node.Declaration> declarations) {
  public static Syntactics of(
    Lectic.Source lectics,
    List<Node.Declaration> declarations)
  {
    return new Syntactics(lectics, List.copyOf(declarations));
  }
}
