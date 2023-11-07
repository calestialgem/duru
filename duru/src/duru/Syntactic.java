package duru;

import java.util.List;

sealed interface Syntactic {
  record Source(Lectic.Source lectics, List<Declaration> declarations)
    implements Syntactic
  {
    public static Source of(
      Lectic.Source lectics,
      List<Declaration> declarations)
    {
      return new Source(lectics, List.copyOf(declarations));
    }
  }

  sealed interface Node extends Syntactic {}

  record Declaration() implements Node {}
}
