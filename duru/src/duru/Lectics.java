package duru;

public record Lectics(
  String text,
  List<Token> tokens,
  List<Location> locations)
{}
