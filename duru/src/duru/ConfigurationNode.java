package duru;

public sealed interface ConfigurationNode {
  sealed interface PackageDeclaration extends ConfigurationNode {
    PackageName name();
  }

  record Executable(Location location, PackageName name)
    implements PackageDeclaration
  {}

  record Library(Location location, PackageName name)
    implements PackageDeclaration
  {}

  record PackageName(
    Location location,
    List<ConfigurationToken.Identifier> subspaces) implements ConfigurationNode
  {
    @Override
    public String toString() {
      var string = new StringBuilder();
      string.append(subspaces.getFirst().text());
      for (var i = 1; i < subspaces.length(); i++) {
        string.append(':');
        string.append(':');
        string.append(subspaces.get(i).text());
      }
      return string.toString();
    }
  }

  Location location();
}
