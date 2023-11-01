package duru.resolution;

import java.util.Map;
import java.util.Optional;

import duru.syntactics.Node;
import duru.syntactics.Syntactics;

/** Tabulated user-defined constructs in a package. */
public record Resolution(
  Optional<Node.Entrypoint> entrypoint,
  Map<String, Node.Definition> globals,
  Map<Node.Declaration, Syntactics> origins)
{
  /** Constructs. */
  public Resolution {
    globals = Map.copyOf(globals);
    origins = Map.copyOf(origins);
  }
}
