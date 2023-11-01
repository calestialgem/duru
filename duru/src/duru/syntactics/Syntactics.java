package duru.syntactics;

import java.util.List;

import duru.diagnostic.Subject;
import duru.lectics.Lectics;
import duru.lectics.Token;

/** Syntactical representation of a source file. */
public record Syntactics(Lectics source, List<Node.Declaration> declarations) {
  /** Constructs. */
  public Syntactics {
    declarations = List.copyOf(declarations);
  }

  /** Returns a subject as a declaration in this source file. */
  public Subject subject(Node.Declaration declaration) {
    return source.subject(representative(declaration));
  }

  /** Returns a declaration's representative. */
  public Token representative(Node.Declaration declaration) {
    return declaration.representative(source.tokens());
  }
}
