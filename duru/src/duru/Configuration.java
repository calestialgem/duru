package duru;

public sealed interface Configuration {
  record Project() implements Configuration {}

  String name = "project.duru";
}
