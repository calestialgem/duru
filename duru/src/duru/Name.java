package duru;

import java.nio.file.Path;

public record Name(List<String> identifiers) {
  public static Name of(String... identifiers) {
    return new Name(List.of(identifiers));
  }

  public String getModule() {
    return identifiers.getFirst();
  }

  public Name getPackage() {
    return new Name(identifiers.sublist(0, identifiers.length() - 1));
  }

  public String getSymbol() {
    return identifiers.getLast();
  }

  public boolean isScoped() {
    return identifiers.length() != 1;
  }

  public Name scope(String identifier) {
    var scoped = ListBuffer.<String>create();
    scoped.addAll(identifiers);
    scoped.add(identifier);
    return new Name(scoped.toList());
  }

  public Path resolve(Path root) {
    for (var i = 1; i < identifiers.length(); i++) {
      root = root.resolve(identifiers.get(i));
    }
    return root;
  }

  public String joined(String separator) {
    var string = new StringBuilder();
    joined(string, separator);
    return string.toString();
  }

  public void joined(StringBuilder string, String separator) {
    string.append(identifiers.getFirst());
    for (var i = 1; i < identifiers.length(); i++) {
      string.append(separator);
      string.append(identifiers.get(i));
    }
  }

  @Override
  public String toString() {
    return joined("::");
  }
}
