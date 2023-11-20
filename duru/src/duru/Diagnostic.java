package duru;

public final class Diagnostic {
  public static RuntimeException failure(
    Object subject,
    Throwable cause,
    String format,
    Object... arguments)
  {
    return diagnose(subject, cause, "failure", format, arguments);
  }

  public static RuntimeException unimplemented(Object subject) {
    return failure(
      subject,
      "unimplemented `%s`",
      new Throwable().getStackTrace()[1]);
  }

  public static RuntimeException failure(
    Object subject,
    String format,
    Object... arguments)
  {
    return diagnose(subject, "failure", format, arguments);
  }

  public static RuntimeException error(
    Object subject,
    String format,
    Object... arguments)
  {
    return diagnose(subject, "error", format, arguments);
  }

  public static RuntimeException diagnose(
    Object subject,
    Throwable cause,
    String title,
    String format,
    Object... arguments)
  {
    return new RuntimeException(
      "%s: %s: %s".formatted(subject, title, format.formatted(arguments)),
      cause);
  }

  public static RuntimeException diagnose(
    Object subject,
    String title,
    String format,
    Object... arguments)
  {
    var name = subject.toString();
    if (name.isEmpty())
      return new RuntimeException(
        "%s: %s".formatted(title, format.formatted(arguments)));
    return new RuntimeException(
      "%s: %s: %s".formatted(name, title, format.formatted(arguments)));
  }
}
