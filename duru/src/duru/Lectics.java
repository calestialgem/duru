package duru;

public record Lectics(
  String text,
  List<TokenType> types,
  List<Location> locations)
{}
