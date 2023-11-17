package duru;

public sealed interface ConfigurationNode {
  record Module(
    Location location,
    ConfigurationToken.Identifier name,
    List<PackageDeclaration> declarations) implements ConfigurationNode
  {}

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
  {}

  Location location();
}
