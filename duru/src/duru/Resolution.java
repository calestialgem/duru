package duru;

public record Resolution(
  String name,
  Map<String, Node.Declaration> declarations)
{}
