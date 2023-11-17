package duru;

public record Subject(String name) {
  private static ListBuffer<Subject> subjects;

  static {
    subjects = ListBuffer.create();
  }

  public static Subject getLast() {
    return subjects.getLast();
  }

  public static void add(Object name) {
    add(new Subject(name.toString()));
  }

  public static void add(Subject subject) {
    subjects.add(subject);
  }

  public static Subject removeLast() {
    return subjects.removeLast();
  }

  public static RuntimeException failure(
    Throwable cause,
    String format,
    Object... arguments)
  {
    return getLast().diagnose("failure", format, arguments).exception(cause);
  }

  public static RuntimeException unimplemented() {
    return failure("unimplemented `%s`", new Throwable().getStackTrace()[1]);
  }

  public static RuntimeException failure(String format, Object... arguments) {
    return getLast().diagnose("failure", format, arguments).exception();
  }

  public static RuntimeException error(String format, Object... arguments) {
    return getLast().diagnose("error", format, arguments).exception();
  }

  public Diagnostic diagnose(String title, String format, Object... arguments) {
    return new Diagnostic(
      "%s: %s: %s".formatted(name, title, format.formatted(arguments)));
  }
}
