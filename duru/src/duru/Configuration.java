package duru;

public record Configuration(
  Map<String, Location> executables,
  Map<String, Location> libraries)
{}
