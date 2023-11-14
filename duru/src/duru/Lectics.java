package duru;

public record Lectics(
  String text,
  List<Tag> tokens,
  List<Location> locations)
{}
