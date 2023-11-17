package duru;

public record Configuration(
  String name,
  Set<String> executables,
  Set<String> exports)
{}
