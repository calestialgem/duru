package duru;

public sealed interface Configuration {
  record Project() implements Configuration {}
}
