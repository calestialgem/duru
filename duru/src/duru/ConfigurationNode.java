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
    List<ConfigurationToken.Identifier> identifiers)
    implements ConfigurationNode
  {
    public Name toName() {
      return new Name(
        identifiers.transform(ConfigurationToken.Identifier::text));
    }

    @Override
    public String toString() {
      return toName().toString();
    }
  }

  Location location();
}
