package duru.lectics;

import java.util.List;

import duru.diagnostic.Subject;
import duru.source.Source;

/** Lexical representation of a source file. */
public record Lectics(Source source, List<Token> tokens) {
  /** Constructs. */
  public Lectics {
    tokens = List.copyOf(tokens);
  }

  /** Returns the token text as a subject. */
  public Subject subject(Token token) {
    return source.subject(token.start(), token.end());
  }
}
