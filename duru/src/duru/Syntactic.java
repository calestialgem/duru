package duru;

sealed interface Syntactic {
  record Source(Lectic.Source lectics, List<Declaration> declarations)
    implements Syntactic
  {}

  sealed interface Node extends Syntactic {}

  record Declaration() implements Node {}
}
