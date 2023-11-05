package duru;

/** Fully qualified name of a global symbol. */
public record Name(Namespace namespace, String identifier) {
  @Override
  public String toString() {
    return "%s.%s".formatted(namespace, identifier);
  }
}
