package duru;

import java.util.List;

record Syntactics(Lectics lectics, List<Node.Declaration> declarations) {
  public static Syntactics of(
    Lectics lectics,
    List<Node.Declaration> declarations)
  {
    return new Syntactics(lectics, List.copyOf(declarations));
  }
}
