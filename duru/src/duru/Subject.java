package duru;

public record Subject(String name) {
  public Diagnostic diagnose(String title, String format, Object... arguments) {
    return new Diagnostic(
      "%s: %s: %s".formatted(name, title, format.formatted(arguments)));
  }
}
