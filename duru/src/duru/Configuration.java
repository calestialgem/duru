package duru;

public record Configuration(
  Map<Name, Location> executables,
  Map<Name, Location> libraries)
{}
