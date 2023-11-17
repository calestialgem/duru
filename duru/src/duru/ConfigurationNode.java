package duru;

public sealed interface ConfigurationNode {
  record Module(
    Location location,
    ConfigurationToken.Identifier name,
    List<Executable> executables) implements ConfigurationNode
  {}

  record Executable(Location location, PackageName name)
    implements ConfigurationNode
  {}

  record PackageName(
    Location location,
    List<ConfigurationToken.Identifier> subspaces) implements ConfigurationNode
  {}

  Location location();
}
