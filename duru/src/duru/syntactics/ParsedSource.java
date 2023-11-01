package duru.syntactics;

import java.util.List;

import duru.lectics.LexedSource;

/** Syntactical representation of a source file. */
public record ParsedSource(
  LexedSource source,
  List<Node.Declaration> declarations)
{
  /** Constructs. */
  public ParsedSource {
    declarations = List.copyOf(declarations);
  }
}
