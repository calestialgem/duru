package duru;

sealed interface Resolution {
  record Package(List<Source> sources) implements Resolution {}

  record Source(Syntactic.Source syntactics) implements Resolution {}
}
