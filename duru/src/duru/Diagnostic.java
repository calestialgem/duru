package duru;

public record Diagnostic(String message) {
  public RuntimeException exception() {
    return new RuntimeException(message);
  }

  public RuntimeException exception(Throwable cause) {
    return new RuntimeException(message, cause);
  }
}
