package duru.lectics;

import java.util.List;

import duru.source.Source;

/** Lexical representation of a source file. */
public record LexedSource(Source source, List<Token> tokens) {
  /** Constructs. */
  public LexedSource {
    tokens = List.copyOf(tokens);
  }
}
