package duru;

public final class Diagnostic extends RuntimeException {
  public static Diagnostic error(String format, Object... arguments) {
    return new Diagnostic("error", format, arguments);
  }

  public static Diagnostic failure(
    Throwable cause,
    String format,
    Object... arguments)
  {
    return new Diagnostic(cause, "failure", format, arguments);
  }

  private Diagnostic(
    Throwable cause,
    String title,
    String format,
    Object... arguments)
  {
    super("%s: %s".formatted(title, format.formatted(arguments)), cause);
  }

  private Diagnostic(String title, String format, Object... arguments) {
    super("%s: %s".formatted(title, format.formatted(arguments)));
  }

  public RuntimeException from(Object subject) {
    return new RuntimeException(
      "%s: %s".formatted(subject, getMessage()),
      this);
  }
}
