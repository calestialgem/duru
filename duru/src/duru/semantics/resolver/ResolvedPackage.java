package duru.semantics.resolver;

import java.util.Map;
import java.util.Optional;

import duru.syntactics.Node;
import duru.syntactics.ParsedSource;

/** Tabulated user-defined constructs in a package. */
public record ResolvedPackage(
  Optional<Node.Entrypoint> entrypoint,
  Map<String, Node.Definition> globals,
  Map<Node.Declaration, ParsedSource> origins)
{
  /** Constructs. */
  public ResolvedPackage {
    globals = Map.copyOf(globals);
    origins = Map.copyOf(origins);
  }
}
