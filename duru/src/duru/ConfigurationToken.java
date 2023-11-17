package duru;

public sealed interface ConfigurationToken {
  record OpeningBrace(Location location) implements ConfigurationToken {}

  record ClosingBrace(Location location) implements ConfigurationToken {}

  record Semicolon(Location location) implements ConfigurationToken {}

  record Dot(Location location) implements ConfigurationToken {}

  record Module(Location location) implements ConfigurationToken {}

  record Executable(Location location) implements ConfigurationToken {}

  record Library(Location location) implements ConfigurationToken {}

  record Identifier(Location location, String text)
    implements ConfigurationToken
  {}

  Location location();
}
